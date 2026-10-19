package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.AppThemeMode
import com.example.data.FileEntity
import com.example.data.FolderEntity
import com.example.data.UserEntity
import com.example.data.UserPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FileFilterType {
    ALL,
    FAVORITES,
    CODE_ONLY,
    TEXT_ONLY
}

enum class SaveStatus {
    SAVED,
    SAVING,
    UNSAVED
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository
    private val preferences: UserPreferences = UserPreferences(application)

    init {
        val database = AppDatabase.getInstance(application)
        repository = AppRepository(database.appDao())
    }

    // --- THEME ---
    private val _themeMode = MutableStateFlow(preferences.themeMode)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        preferences.themeMode = mode
        _themeMode.value = mode
    }

    // --- AUTH & USER ---
    private val _currentUserId = MutableStateFlow(preferences.currentUserId)
    val currentUserId: StateFlow<Long> = _currentUserId.asStateFlow()

    val currentUser: StateFlow<UserEntity?> = _currentUserId.flatMapLatest { userId ->
        if (userId > 0) repository.observeUser(userId) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    fun clearAuthError() {
        _authError.value = null
    }

    fun login(username: String, passwordPlain: String) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.findUserByUsername(username)
            if (user == null) {
                _authError.value = "Username tidak ditemukan di database offline."
            } else if (user.passwordHash != passwordPlain) {
                _authError.value = "Password salah. Silakan coba lagi."
            } else {
                preferences.currentUserId = user.id
                _currentUserId.value = user.id
                _currentFolderId.value = null
                _folderBreadcrumbs.value = emptyList()
            }
        }
    }

    fun register(username: String, passwordPlain: String, displayName: String, avatarUri: String? = null, presetId: Int = 1) {
        viewModelScope.launch {
            _authError.value = null
            if (username.isBlank() || passwordPlain.isBlank()) {
                _authError.value = "Username dan password tidak boleh kosong."
                return@launch
            }
            val existing = repository.findUserByUsername(username)
            if (existing != null) {
                _authError.value = "Username '$username' sudah terdaftar."
                return@launch
            }
            try {
                val persistentAvatar = avatarUri?.let {
                    com.example.util.LocalAssetManager.persistMediaLocally(
                        getApplication(),
                        it,
                        "internal_avatars",
                        "new_user_avatar"
                    )
                }
                val userId = repository.registerUser(username, passwordPlain, displayName, persistentAvatar, presetId)
                preferences.currentUserId = userId
                _currentUserId.value = userId
                _currentFolderId.value = null
                _folderBreadcrumbs.value = emptyList()
            } catch (e: Exception) {
                _authError.value = "Gagal mendaftar: ${e.message}"
            }
        }
    }

    fun logout() {
        preferences.currentUserId = -1L
        _currentUserId.value = -1L
        _activeEditingFile.value = null
        _currentFolderId.value = null
        _folderBreadcrumbs.value = emptyList()
    }

    fun updateProfile(displayName: String, avatarUri: String?, presetId: Int) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val persistentAvatar = avatarUri?.let {
                com.example.util.LocalAssetManager.persistMediaLocally(
                    getApplication(),
                    it,
                    "internal_avatars",
                    "user_${user.id}_avatar"
                )
            } ?: user.avatarUri

            val updated = user.copy(
                displayName = displayName.ifBlank { user.displayName },
                avatarUri = persistentAvatar,
                avatarPresetId = presetId
            )
            repository.updateUserProfile(updated)
        }
    }

    // --- FOLDER NAVIGATION & HIERARCHY ---
    private val _currentFolderId = MutableStateFlow<Long?>(null)
    val currentFolderId: StateFlow<Long?> = _currentFolderId.asStateFlow()

    private val _folderBreadcrumbs = MutableStateFlow<List<FolderEntity>>(emptyList())
    val folderBreadcrumbs: StateFlow<List<FolderEntity>> = _folderBreadcrumbs.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(FileFilterType.ALL)
    val filterType: StateFlow<FileFilterType> = _filterType.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(filter: FileFilterType) {
        _filterType.value = filter
    }

    val currentFolders: StateFlow<List<FolderEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId <= 0) flowOf(emptyList())
        else _currentFolderId.flatMapLatest { folderId ->
            if (folderId == null) repository.getRootFolders(userId)
            else repository.getSubFolders(userId, folderId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFiles: StateFlow<List<FileEntity>> = _currentUserId.flatMapLatest { userId ->
        if (userId <= 0) flowOf(emptyList())
        else _currentFolderId.flatMapLatest { folderId ->
            if (folderId == null) repository.getRootFiles(userId)
            else repository.getFilesByFolder(userId, folderId)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun navigateToFolder(folder: FolderEntity) {
        _currentFolderId.value = folder.id
        _folderBreadcrumbs.value = _folderBreadcrumbs.value + folder
    }

    fun navigateBackFolder(): Boolean {
        val crumbs = _folderBreadcrumbs.value
        return if (crumbs.isNotEmpty()) {
            val updated = crumbs.dropLast(1)
            _folderBreadcrumbs.value = updated
            _currentFolderId.value = updated.lastOrNull()?.id
            true
        } else {
            false
        }
    }

    fun navigateToBreadcrumb(targetIndex: Int) {
        val crumbs = _folderBreadcrumbs.value
        if (targetIndex < 0) {
            _folderBreadcrumbs.value = emptyList()
            _currentFolderId.value = null
        } else if (targetIndex < crumbs.size) {
            val updated = crumbs.take(targetIndex + 1)
            _folderBreadcrumbs.value = updated
            _currentFolderId.value = updated.last().id
        }
    }

    fun createFolder(name: String, colorHex: String) {
        val userId = currentUserId.value
        if (userId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.createFolder(
                userId = userId,
                name = name,
                parentId = currentFolderId.value,
                colorHex = colorHex
            )
        }
    }

    fun updateFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder)
        }
    }

    fun deleteFolder(folderId: Long) {
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_currentFolderId.value == folderId) {
                navigateBackFolder()
            }
        }
    }

    fun toggleFavoriteFolder(folder: FolderEntity) {
        viewModelScope.launch {
            repository.updateFolder(folder.copy(isFavorite = !folder.isFavorite))
        }
    }

    fun createFile(name: String, extension: String, colorHex: String, content: String = "") {
        val userId = currentUserId.value
        if (userId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            val fileId = repository.createFile(
                userId = userId,
                folderId = currentFolderId.value,
                name = name,
                extension = extension,
                content = content,
                colorHex = colorHex
            )
            // Auto open newly created file
            val file = repository.getFileById(fileId)
            if (file != null) {
                openFileForEditing(file)
            }
        }
    }

    fun deleteFile(fileId: Long) {
        viewModelScope.launch {
            repository.deleteFile(fileId)
            if (_activeEditingFile.value?.id == fileId) {
                closeEditor()
            }
        }
    }

    fun toggleFavoriteFile(file: FileEntity) {
        viewModelScope.launch {
            repository.updateFile(file.copy(isFavorite = !file.isFavorite))
        }
    }

    fun updateFileProperties(file: FileEntity, newName: String, newColorHex: String) {
        viewModelScope.launch {
            val updated = file.copy(
                name = newName.trim(),
                colorHex = newColorHex
            )
            repository.updateFile(updated)
            if (_activeEditingFile.value?.id == file.id) {
                _activeEditingFile.value = updated
            }
        }
    }

    // --- ACTIVE FILE EDITOR & AUTOSAVE ---
    private val _activeEditingFile = MutableStateFlow<FileEntity?>(null)
    val activeEditingFile: StateFlow<FileEntity?> = _activeEditingFile.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _saveStatus = MutableStateFlow(SaveStatus.SAVED)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _isWordWrap = MutableStateFlow(preferences.isWordWrapEnabled)
    val isWordWrap: StateFlow<Boolean> = _isWordWrap.asStateFlow()

    private val _customBgUri = MutableStateFlow(preferences.customBackgroundUri)
    val customBgUri: StateFlow<String?> = _customBgUri.asStateFlow()

    private val _bgOpacity = MutableStateFlow(preferences.backgroundOpacity)
    val bgOpacity: StateFlow<Float> = _bgOpacity.asStateFlow()

    private var autoSaveJob: Job? = null

    fun openFileForEditing(file: FileEntity) {
        _activeEditingFile.value = file
        _editorContent.value = file.content
        _saveStatus.value = SaveStatus.SAVED
    }

    fun closeEditor() {
        autoSaveJob?.cancel()
        val fileToSave = _activeEditingFile.value
        val textToSave = _editorContent.value
        // Immediately clear editing state so UI transitions back to explorer immediately
        _activeEditingFile.value = null
        _editorContent.value = ""
        _saveStatus.value = SaveStatus.SAVED

        // Persist content to repository without resurrecting _activeEditingFile
        if (fileToSave != null) {
            viewModelScope.launch {
                val updated = fileToSave.copy(
                    content = textToSave,
                    updatedAt = System.currentTimeMillis()
                )
                repository.updateFile(updated)
            }
        }
    }

    fun closeEditorToRoot() {
        closeEditor()
        _currentFolderId.value = null
        _folderBreadcrumbs.value = emptyList()
    }

    fun onEditorContentChange(newText: String) {
        _editorContent.value = newText
        _saveStatus.value = SaveStatus.UNSAVED

        // Debounced Auto-Save (800ms)
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            delay(800L)
            saveCurrentFileImmediately()
        }
    }

    fun toggleWordWrap() {
        val next = !_isWordWrap.value
        _isWordWrap.value = next
        preferences.isWordWrapEnabled = next
    }

    fun setCustomBackground(uri: String?) {
        viewModelScope.launch {
            val persistentPath = uri?.let {
                com.example.util.LocalAssetManager.persistMediaLocally(
                    getApplication(),
                    it,
                    "internal_wallpapers",
                    "editor_bg"
                )
            }
            preferences.customBackgroundUri = persistentPath
            _customBgUri.value = persistentPath
        }
    }

    fun setBackgroundOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.05f, 0.95f)
        preferences.backgroundOpacity = clamped
        _bgOpacity.value = clamped
    }

    fun saveCurrentFileImmediately() {
        val file = _activeEditingFile.value ?: return
        val currentText = _editorContent.value
        viewModelScope.launch {
            _saveStatus.value = SaveStatus.SAVING
            val updated = file.copy(
                content = currentText,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateFile(updated)
            // CRITICAL FIX: Only update _activeEditingFile if the user is STILL in editor!
            if (_activeEditingFile.value?.id == file.id) {
                _activeEditingFile.value = updated
                _saveStatus.value = SaveStatus.SAVED
            }
        }
    }

    // Called on App Lifecycle onPause/onStop
    fun onAppPaused() {
        saveCurrentFileImmediately()
    }

    // --- AUTO COPY-PASTE CLIPBOARD BUFFER ---
    private val _clipboardHistory = MutableStateFlow<List<String>>(
        listOf(
            "console.log('Falambons Code');",
            "Credit by : falambons developer",
            "#00E5FF",
            "display: flex;"
        )
    )
    val clipboardHistory: StateFlow<List<String>> = _clipboardHistory.asStateFlow()

    fun addToClipboardHistory(text: String) {
        if (text.isBlank()) return
        val updated = (listOf(text) + _clipboardHistory.value.filter { it != text }).take(15)
        _clipboardHistory.value = updated
    }

    // --- FLOATING CODE PAD ---
    private val _isFloatingPadVisible = MutableStateFlow(preferences.isFloatingPadEnabled)
    val isFloatingPadVisible: StateFlow<Boolean> = _isFloatingPadVisible.asStateFlow()

    private val _floatingPadText = MutableStateFlow(preferences.floatingPadText)
    val floatingPadText: StateFlow<String> = _floatingPadText.asStateFlow()

    fun toggleFloatingPad() {
        val next = !_isFloatingPadVisible.value
        _isFloatingPadVisible.value = next
        preferences.isFloatingPadEnabled = next
    }

    fun updateFloatingPadText(text: String) {
        _floatingPadText.value = text
        preferences.floatingPadText = text
    }

    // --- IMPORT FILE ---
    fun importExternalFile(fileName: String, content: String) {
        val userId = currentUserId.value
        if (userId <= 0) return
        val ext = fileName.substringAfterLast('.', "txt")
        val baseName = fileName.substringBeforeLast('.')
        createFile(
            name = baseName.ifBlank { "imported_file" },
            extension = ext,
            colorHex = "#38BDF8",
            content = content
        )
    }
}
