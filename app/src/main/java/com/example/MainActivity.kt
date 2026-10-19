package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.components.FloatingCodePad
import com.example.ui.components.ProfileDialog
import com.example.ui.components.QrCodeDialog
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CodeEditorScreen
import coil.Coil
import coil.compose.LocalImageLoader
import com.example.ui.screens.FolderExplorerScreen
import com.example.ui.theme.FalambonsCodeTheme
import com.example.util.GifImageLoader
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gifLoader = GifImageLoader.get(this)
        Coil.setImageLoader(gifLoader)

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

            CompositionLocalProvider(LocalImageLoader provides gifLoader) {
                FalambonsCodeTheme(themeMode = themeMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        FalambonsApp(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onAppPaused()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppPaused()
    }
}

@Composable
fun FalambonsApp(viewModel: MainViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.onAppPaused()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authError by viewModel.authError.collectAsStateWithLifecycle()
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    val currentFolders by viewModel.currentFolders.collectAsStateWithLifecycle()
    val currentFiles by viewModel.currentFiles.collectAsStateWithLifecycle()
    val breadcrumbs by viewModel.folderBreadcrumbs.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.filterType.collectAsStateWithLifecycle()

    val activeFile by viewModel.activeEditingFile.collectAsStateWithLifecycle()
    val editorContent by viewModel.editorContent.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val isWordWrap by viewModel.isWordWrap.collectAsStateWithLifecycle()
    val customBgUri by viewModel.customBgUri.collectAsStateWithLifecycle()
    val bgOpacity by viewModel.bgOpacity.collectAsStateWithLifecycle()
    val clipboardHistory by viewModel.clipboardHistory.collectAsStateWithLifecycle()

    val isFloatingPadVisible by viewModel.isFloatingPadVisible.collectAsStateWithLifecycle()
    val floatingPadText by viewModel.floatingPadText.collectAsStateWithLifecycle()

    var showProfileDialog by remember { mutableStateOf(false) }
    var qrDialogState by remember { mutableStateOf<Pair<String, String>?>(null) } // content, title

    Box(modifier = Modifier.fillMaxSize()) {
        val user = currentUser
        if (user == null) {
            AuthScreen(
                errorMessage = authError,
                onLogin = { u, p -> viewModel.login(u, p) },
                onRegister = { u, p, name, avatar, preset ->
                    viewModel.register(u, p, name, avatar, preset)
                },
                onDismissError = { viewModel.clearAuthError() }
            )
        } else {
            val fileToEdit = activeFile
            if (fileToEdit != null) {
                CodeEditorScreen(
                    file = fileToEdit,
                    content = editorContent,
                    saveStatus = saveStatus,
                    isWordWrap = isWordWrap,
                    customBgUri = customBgUri,
                    bgOpacity = bgOpacity,
                    clipboardHistory = clipboardHistory,
                    onContentChange = { viewModel.onEditorContentChange(it) },
                    onSaveImmediately = { viewModel.saveCurrentFileImmediately() },
                    onToggleWordWrap = { viewModel.toggleWordWrap() },
                    onSetCustomBackground = { uri, op ->
                        viewModel.setCustomBackground(uri)
                        viewModel.setBackgroundOpacity(op)
                    },
                    onAddClipboard = { viewModel.addToClipboardHistory(it) },
                    onOpenQrDialog = { text, title ->
                        qrDialogState = Pair(text, title)
                    },
                    onClose = { viewModel.closeEditor() },
                    onCloseToRoot = { viewModel.closeEditorToRoot() }
                )
            } else {
                FolderExplorerScreen(
                    user = user,
                    folders = currentFolders,
                    files = currentFiles,
                    breadcrumbs = breadcrumbs,
                    searchQuery = searchQuery,
                    filterType = filterType,
                    currentTheme = currentTheme,
                    isFloatingPadActive = isFloatingPadVisible,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setFilterType(it) },
                    onNavigateToFolder = { viewModel.navigateToFolder(it) },
                    onNavigateBack = { viewModel.navigateBackFolder() },
                    onNavigateBreadcrumb = { viewModel.navigateToBreadcrumb(it) },
                    onCreateFolder = { name, color -> viewModel.createFolder(name, color) },
                    onUpdateFolder = { viewModel.updateFolder(it) },
                    onDeleteFolder = { viewModel.deleteFolder(it) },
                    onToggleFavoriteFolder = { viewModel.toggleFavoriteFolder(it) },
                    onCreateFile = { name, ext, color -> viewModel.createFile(name, ext, color) },
                    onOpenFile = { viewModel.openFileForEditing(it) },
                    onDeleteFile = { viewModel.deleteFile(it) },
                    onToggleFavoriteFile = { viewModel.toggleFavoriteFile(it) },
                    onUpdateFileProperties = { file, name, color ->
                        viewModel.updateFileProperties(file, name, color)
                    },
                    onImportFile = { name, content -> viewModel.importExternalFile(name, content) },
                    onSwitchTheme = { viewModel.setThemeMode(it) },
                    onOpenProfile = { showProfileDialog = true },
                    onOpenQrDialog = { text, title ->
                        qrDialogState = Pair(text, title)
                    },
                    onToggleFloatingPad = { viewModel.toggleFloatingPad() }
                )
            }

            // Draggable Floating Pad Overlay (visible across explorer & editor if enabled)
            if (isFloatingPadVisible) {
                FloatingCodePad(
                    text = floatingPadText,
                    onTextChange = { viewModel.updateFloatingPadText(it) },
                    onClose = { viewModel.toggleFloatingPad() }
                )
            }

            // Profile Dialog
            if (showProfileDialog) {
                ProfileDialog(
                    user = user,
                    onUpdateProfile = { name, avatar, preset ->
                        viewModel.updateProfile(name, avatar, preset)
                    },
                    onLogout = {
                        viewModel.logout()
                    },
                    onDismiss = { showProfileDialog = false }
                )
            }

            // Custom QR Dialog
            qrDialogState?.let { (content, title) ->
                QrCodeDialog(
                    initialText = content,
                    title = title,
                    onDismiss = { qrDialogState = null }
                )
            }
        }
    }
}
