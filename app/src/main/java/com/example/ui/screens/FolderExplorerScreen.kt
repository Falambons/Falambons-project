package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppThemeMode
import com.example.data.FileEntity
import com.example.data.FolderEntity
import com.example.data.UserEntity
import com.example.ui.FileFilterType
import com.example.ui.components.UserAvatar
import com.example.ui.theme.RgbNeonCyan
import com.example.ui.theme.RgbNeonGreen
import com.example.ui.theme.RgbNeonPink
import com.example.util.FileShareHelper
import com.example.util.PdfExporter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val PRESET_COLORS = listOf(
    Pair("Cyan", "#00E5FF"),
    Pair("Pink", "#FF007F"),
    Pair("Green", "#22C55E"),
    Pair("Amber", "#F59E0B"),
    Pair("Purple", "#A855F7"),
    Pair("Blue", "#38BDF8"),
    Pair("Red", "#EF4444")
)

val SUPPORTED_EXTENSIONS = listOf(
    "js", "css", "cpp", "php", "html", "txt", "py", "json", "java", "sql"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderExplorerScreen(
    user: UserEntity,
    folders: List<FolderEntity>,
    files: List<FileEntity>,
    breadcrumbs: List<FolderEntity>,
    searchQuery: String,
    filterType: FileFilterType,
    currentTheme: AppThemeMode,
    isFloatingPadActive: Boolean,
    onSearchChange: (String) -> Unit,
    onFilterChange: (FileFilterType) -> Unit,
    onNavigateToFolder: (FolderEntity) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateBreadcrumb: (Int) -> Unit,
    onCreateFolder: (String, String) -> Unit,
    onUpdateFolder: (FolderEntity) -> Unit,
    onDeleteFolder: (Long) -> Unit,
    onToggleFavoriteFolder: (FolderEntity) -> Unit,
    onCreateFile: (String, String, String) -> Unit,
    onOpenFile: (FileEntity) -> Unit,
    onDeleteFile: (Long) -> Unit,
    onToggleFavoriteFile: (FileEntity) -> Unit,
    onUpdateFileProperties: (FileEntity, String, String) -> Unit,
    onImportFile: (String, String) -> Unit,
    onSwitchTheme: (AppThemeMode) -> Unit,
    onOpenProfile: () -> Unit,
    onOpenQrDialog: (String, String) -> Unit,
    onToggleFloatingPad: () -> Unit
) {
    val context = LocalContext.current
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateFileDialog by remember { mutableStateOf(false) }

    // Import file launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val reader = BufferedReader(InputStreamReader(stream))
                    val content = reader.readText()
                    val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "imported_code.txt"
                    onImportFile(fileName, content)
                }
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Falambons Code",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Credit by : falambons developer",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    // Theme Switcher Button (Pure Dark -> Blue Dark -> RGB)
                    IconButton(
                        onClick = {
                            val next = when (currentTheme) {
                                AppThemeMode.PURE_DARK -> AppThemeMode.BLUE_DARK
                                AppThemeMode.BLUE_DARK -> AppThemeMode.RGB
                                AppThemeMode.RGB -> AppThemeMode.PURE_DARK
                            }
                            onSwitchTheme(next)
                        },
                        modifier = Modifier.testTag("btn_switch_theme")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Ganti Tema (${currentTheme.name})",
                            tint = when (currentTheme) {
                                AppThemeMode.PURE_DARK -> Color.White
                                AppThemeMode.BLUE_DARK -> Color(0xFF00E5FF)
                                AppThemeMode.RGB -> Color(0xFFFF007F)
                            }
                        )
                    }

                    // Floating Code Pad Toggle
                    IconButton(
                        onClick = onToggleFloatingPad,
                        modifier = Modifier.testTag("btn_toggle_floating")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Floating Pad",
                            tint = if (isFloatingPadActive) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Import File Button
                    IconButton(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.testTag("btn_import_file")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Import File",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // User Profile Button
                    Box(modifier = Modifier.padding(end = 8.dp)) {
                        IconButton(onClick = onOpenProfile, modifier = Modifier.testTag("btn_profile_avatar")) {
                            UserAvatar(
                                avatarUri = user.avatarUri,
                                presetId = user.avatarPresetId,
                                size = 32.dp
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FloatingActionButton(
                    onClick = { showCreateFolderDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_create_folder")
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = "Tambah Folder")
                }

                FloatingActionButton(
                    onClick = { showCreateFileDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_create_file")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat File", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Breadcrumbs Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📁 Root",
                    fontWeight = if (breadcrumbs.isEmpty()) FontWeight.Bold else FontWeight.Normal,
                    color = if (breadcrumbs.isEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { onNavigateBreadcrumb(-1) }
                        .padding(4.dp)
                )

                breadcrumbs.forEachIndexed { index, folder ->
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = folder.name,
                        fontWeight = if (index == breadcrumbs.lastIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (index == breadcrumbs.lastIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { onNavigateBreadcrumb(index) }
                            .padding(4.dp)
                    )
                }
            }

            // Search Bar & Filter Chips
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Cari folder atau file (JS, CSS, C++, PHP, TXT)...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("input_search"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filterType == FileFilterType.ALL,
                        onClick = { onFilterChange(FileFilterType.ALL) },
                        label = { Text("Semua") }
                    )
                    FilterChip(
                        selected = filterType == FileFilterType.FAVORITES,
                        onClick = { onFilterChange(FileFilterType.FAVORITES) },
                        label = { Text("⭐ Favorit") }
                    )
                    FilterChip(
                        selected = filterType == FileFilterType.CODE_ONLY,
                        onClick = { onFilterChange(FileFilterType.CODE_ONLY) },
                        label = { Text("💻 Code File") }
                    )
                    FilterChip(
                        selected = filterType == FileFilterType.TEXT_ONLY,
                        onClick = { onFilterChange(FileFilterType.TEXT_ONLY) },
                        label = { Text("📄 Teks .txt") }
                    )
                }
            }

            // Folders and Files List
            val filteredFolders = folders.filter {
                (searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)) &&
                        (filterType != FileFilterType.FAVORITES || it.isFavorite) &&
                        (filterType != FileFilterType.CODE_ONLY) &&
                        (filterType != FileFilterType.TEXT_ONLY)
            }

            val filteredFiles = files.filter { file ->
                val matchesQuery = searchQuery.isBlank() ||
                        file.name.contains(searchQuery, ignoreCase = true) ||
                        file.extension.contains(searchQuery, ignoreCase = true)

                val matchesFilter = when (filterType) {
                    FileFilterType.ALL -> true
                    FileFilterType.FAVORITES -> file.isFavorite
                    FileFilterType.CODE_ONLY -> file.extension in listOf("js", "css", "cpp", "php", "html", "py", "json", "java", "sql")
                    FileFilterType.TEXT_ONLY -> file.extension == "txt"
                }

                matchesQuery && matchesFilter
            }

            if (filteredFolders.isEmpty() && filteredFiles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Tidak ada folder atau file yang cocok." else "Folder ini kosong.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tekan tombol + di bawah untuk membuat file/folder",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Folders Section
                    if (filteredFolders.isNotEmpty()) {
                        item {
                            Text(
                                text = "FOLDER (${filteredFolders.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(filteredFolders, key = { "folder_${it.id}" }) { folder ->
                            FolderItemCard(
                                folder = folder,
                                onOpen = { onNavigateToFolder(folder) },
                                onToggleFavorite = { onToggleFavoriteFolder(folder) },
                                onDelete = { onDeleteFolder(folder.id) },
                                onUpdate = { onUpdateFolder(it) }
                            )
                        }
                    }

                    // Files Section
                    if (filteredFiles.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "FILE & KODE (${filteredFiles.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        items(filteredFiles, key = { "file_${it.id}" }) { file ->
                            FileItemCard(
                                file = file,
                                onOpen = { onOpenFile(file) },
                                onToggleFavorite = { onToggleFavoriteFile(file) },
                                onDelete = { onDeleteFile(file.id) },
                                onUpdateProperties = { name, color -> onUpdateFileProperties(file, name, color) },
                                onExportPdf = {
                                    val pdfFile = PdfExporter.exportCodeToPdf(context, file.fullName, file.content)
                                    FileShareHelper.shareFile(context, pdfFile, "application/pdf")
                                },
                                onShareCode = {
                                    FileShareHelper.shareText(context, file.fullName, file.content)
                                },
                                onOpenQr = {
                                    onOpenQrDialog(file.content, "QR Code: ${file.fullName}")
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Create Folder Dialog
    if (showCreateFolderDialog) {
        CreateFolderDialog(
            onCreate = { name, color ->
                onCreateFolder(name, color)
                showCreateFolderDialog = false
            },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    // Create File Dialog
    if (showCreateFileDialog) {
        CreateFileDialog(
            onCreate = { name, ext, color ->
                onCreateFile(name, ext, color)
                showCreateFileDialog = false
            },
            onDismiss = { showCreateFileDialog = false }
        )
    }
}

@Composable
fun FolderItemCard(
    folder: FolderEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (FolderEntity) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    val folderColor = remember(folder.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(folder.colorHex))
        } catch (_: Exception) {
            Color(0xFF00E5FF)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .border(1.dp, folderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(folderColor.copy(alpha = 0.15f))
                    .border(1.5.dp, folderColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = folderColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = folder.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Folder Proyek",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (folder.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorit",
                    tint = if (folder.isFavorite) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Buka Folder") },
                        onClick = {
                            showMenu = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ubah Nama / Warna") },
                        onClick = {
                            showMenu = false
                            showEditDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Folder", color = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        EditFolderDialog(
            folder = folder,
            onSave = { updated ->
                onUpdate(updated)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }
}

@Composable
fun FileItemCard(
    file: FileEntity,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
    onUpdateProperties: (String, String) -> Unit,
    onExportPdf: () -> Unit,
    onShareCode: () -> Unit,
    onOpenQr: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }

    val fileColor = remember(file.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(file.colorHex))
        } catch (_: Exception) {
            Color(0xFF38BDF8)
        }
    }

    val updatedTimeStr = remember(file.updatedAt) {
        SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(Date(file.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Extension Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(fileColor.copy(alpha = 0.15f))
                    .border(1.5.dp, fileColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ".${file.extension.take(3)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = fileColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ukuran: ${file.content.length} karakter • $updatedTimeStr",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (file.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorit",
                    tint = if (file.isFavorite) Color(0xFFFFD700) else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Buka & Edit") },
                        onClick = {
                            showMenu = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Export ke PDF") },
                        leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onExportPdf()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Generate Custom QR") },
                        leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        onClick = {
                            showMenu = false
                            onOpenQr()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Bagikan Teks") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onShareCode()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Ganti Nama & Warna") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            showRenameDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus File", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }

    if (showRenameDialog) {
        EditFileDialog(
            file = file,
            onSave = { name, color ->
                onUpdateProperties(name, color)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}

@Composable
fun CreateFolderDialog(
    onCreate: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf(PRESET_COLORS[0].second) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Buat Folder Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Folder") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_folder_name")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Pilih Warna Folder:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PRESET_COLORS.forEach { pair ->
                        val color = Color(android.graphics.Color.parseColor(pair.second))
                        val isSelected = selectedColorHex == pair.second
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = pair.second }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { if (name.isNotBlank()) onCreate(name, selectedColorHex) },
                        modifier = Modifier.testTag("btn_confirm_create_folder")
                    ) {
                        Text("Buat", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditFolderDialog(
    folder: FolderEntity,
    onSave: (FolderEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(folder.name) }
    var selectedColorHex by remember { mutableStateOf(folder.colorHex) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit Folder", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Folder") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))
                Text("Pilih Warna Folder:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PRESET_COLORS.forEach { pair ->
                        val color = Color(android.graphics.Color.parseColor(pair.second))
                        val isSelected = selectedColorHex == pair.second
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = pair.second }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (name.isNotBlank()) onSave(folder.copy(name = name, colorHex = selectedColorHex))
                    }) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreateFileDialog(
    onCreate: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedExt by remember { mutableStateOf("js") }
    var selectedColorHex by remember { mutableStateOf(PRESET_COLORS[0].second) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Buat File Coding Baru", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama File (tanpa ekstensi)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_file_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Format / Ekstensi Kode:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SUPPORTED_EXTENSIONS.forEach { ext ->
                        val isSelected = ext == selectedExt
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clickable { selectedExt = ext }
                                .padding(2.dp)
                        ) {
                            Text(
                                text = ".$ext",
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Warna Tag File:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PRESET_COLORS.forEach { pair ->
                        val color = Color(android.graphics.Color.parseColor(pair.second))
                        val isSelected = selectedColorHex == pair.second
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = pair.second }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { if (name.isNotBlank()) onCreate(name, selectedExt, selectedColorHex) },
                        modifier = Modifier.testTag("btn_confirm_create_file")
                    ) {
                        Text("Buat & Coding", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditFileDialog(
    file: FileEntity,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(file.name) }
    var selectedColorHex by remember { mutableStateOf(file.colorHex) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Edit File: ${file.fullName}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama File") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text("Pilih Warna Tag:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PRESET_COLORS.forEach { pair ->
                        val color = Color(android.graphics.Color.parseColor(pair.second))
                        val isSelected = selectedColorHex == pair.second
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = pair.second }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Batal") }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = {
                        if (name.isNotBlank()) onSave(name, selectedColorHex)
                    }) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
