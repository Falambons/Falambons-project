package com.example.ui.screens

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.FileEntity
import com.example.syntax.SnippetProvider
import com.example.syntax.SyntaxHighlighter
import com.example.ui.SaveStatus
import com.example.ui.components.CustomBackgroundDialog
import com.example.ui.theme.LineNumberColor
import com.example.util.FileShareHelper
import com.example.util.PdfExporter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    file: FileEntity,
    content: String,
    saveStatus: SaveStatus,
    isWordWrap: Boolean,
    customBgUri: String?,
    bgOpacity: Float,
    clipboardHistory: List<String>,
    onContentChange: (String) -> Unit,
    onSaveImmediately: () -> Unit,
    onToggleWordWrap: () -> Unit,
    onSetCustomBackground: (String?, Float) -> Unit,
    onAddClipboard: (String) -> Unit,
    onOpenQrDialog: (String, String) -> Unit,
    onClose: () -> Unit,
    onCloseToRoot: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showMenu by remember { mutableStateOf(false) }
    var showBgDialog by remember { mutableStateOf(false) }
    var showSnippetTray by remember { mutableStateOf(false) }

    // Clean back handling - closes editor and returns to folder explorer
    BackHandler {
        onClose()
    }

    val visualTransformation = remember(file.extension) {
        SyntaxHighlighter.createVisualTransformation(file.extension)
    }

    val languageSnippets = remember(file.extension) {
        SnippetProvider.getSnippetsForExtension(file.extension)
    }

    val fileColor = remember(file.colorHex) {
        try {
            Color(android.graphics.Color.parseColor(file.colorHex))
        } catch (_: Exception) {
            Color(0xFF38BDF8)
        }
    }

    val lineCount = remember(content) {
        content.count { it == '\n' } + 1
    }

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_editor_back")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali ke Folder")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(fileColor.copy(alpha = 0.2f))
                                .border(1.dp, fileColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = ".${file.extension}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = fileColor
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = file.fullName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1
                            )

                            // Autosave Status Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val statusDotColor = when (saveStatus) {
                                    SaveStatus.SAVED -> Color(0xFF22C55E)
                                    SaveStatus.SAVING -> Color(0xFF00E5FF)
                                    SaveStatus.UNSAVED -> Color(0xFFF59E0B)
                                }
                                val statusText = when (saveStatus) {
                                    SaveStatus.SAVED -> "Tersimpan"
                                    SaveStatus.SAVING -> "Menyimpan..."
                                    SaveStatus.UNSAVED -> "Belum disimpan"
                                }

                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(statusDotColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = statusText,
                                    fontSize = 10.sp,
                                    color = statusDotColor
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Word Wrap / Unlimited Long Line Mode Toggle
                    IconButton(
                        onClick = onToggleWordWrap,
                        modifier = Modifier.testTag("btn_toggle_word_wrap")
                    ) {
                        Icon(
                            imageVector = Icons.Default.WrapText,
                            contentDescription = if (isWordWrap) "Mode Baris Bungkus (Wrap)" else "Mode Unlimited Baris Panjang",
                            tint = if (isWordWrap) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Custom Wallpaper / Background
                    IconButton(
                        onClick = { showBgDialog = true },
                        modifier = Modifier.testTag("btn_custom_background")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallpaper,
                            contentDescription = "Custom Background GIF/Image",
                            tint = if (customBgUri != null) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }

                    // Save Button
                    IconButton(
                        onClick = onSaveImmediately,
                        modifier = Modifier.testTag("btn_manual_save")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Simpan Sekarang",
                            tint = if (saveStatus == SaveStatus.SAVED) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Overflow Menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu Lainnya")
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Export ke PDF") },
                                leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444)) },
                                onClick = {
                                    showMenu = false
                                    val pdf = PdfExporter.exportCodeToPdf(context, file.fullName, content)
                                    FileShareHelper.shareFile(context, pdf, "application/pdf")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Compile ke Custom QR") },
                                leadingIcon = { Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showMenu = false
                                    onOpenQrDialog(content, "QR Code: ${file.fullName}")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Bagikan Teks Kode") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    FileShareHelper.shareText(context, file.fullName, content)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Tutup & Kembali ke Folder") },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showMenu = false
                                    onClose()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Kembali ke Home Root") },
                                leadingIcon = { Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFF00E5FF)) },
                                onClick = {
                                    showMenu = false
                                    onCloseToRoot()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main Coding Area (Line Numbers + Code Input) with optional custom background image / GIF
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Custom Background Image / GIF Layer
                if (!customBgUri.isNullOrEmpty()) {
                    AsyncImage(
                        model = customBgUri,
                        imageLoader = com.example.util.GifImageLoader.get(context),
                        contentDescription = "Custom Coding Wallpaper (GIF/Gambar)",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(bgOpacity)
                    )
                }

                // Code Editor with Line Numbers
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    // Line Numbers Column
                    val lineNumbersText = (1..lineCount).joinToString("\n")
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(Color(0x66000000))
                            .border(width = 0.5.dp, color = Color(0xFF26262B))
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = lineNumbersText,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = LineNumberColor
                            )
                        )
                    }

                    // Code Input Box (Unlimited horizontal scroll vs wrap)
                    val horizontalModifier = if (!isWordWrap) {
                        Modifier.horizontalScroll(horizontalScrollState)
                    } else {
                        Modifier
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .then(horizontalModifier)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = "// Ketik atau tempel kode ${file.extension.uppercase()} di sini...\n// Falambons Developer Code Studio",
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = Color(0xFF555566)
                                )
                            )
                        }

                        BasicTextField(
                            value = content,
                            onValueChange = onContentChange,
                            visualTransformation = visualTransformation,
                            textStyle = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(min = 600.dp)
                                .testTag("input_code_editor")
                        )
                    }
                }
            }

            // FAST CODING & AUTO COPY-PASTE TOOLBAR
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Row 1: Fast Quick Symbols
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Tab button (inserts 4 spaces)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onContentChange(content + "    ") }
                        ) {
                            Text(
                                text = "TAB",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        SnippetProvider.quickSymbols.forEach { sym ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { onContentChange(content + sym) }
                            ) {
                                Text(
                                    text = sym,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: Auto Copy-Paste Bar & Language Snippets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick 1-Tap Copy All
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(content))
                                    onAddClipboard(content)
                                }
                                .testTag("btn_copy_all")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Semua", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                            }
                        }

                        // Quick 1-Tap Paste
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .clickable {
                                    clipboardManager.getText()?.text?.let { clip ->
                                        onContentChange(content + clip)
                                    }
                                }
                                .testTag("btn_paste")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.ContentPaste, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                            }
                        }

                        // Snippets for current extension
                        languageSnippets.forEach { snip ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    onContentChange(content + snip.code)
                                }
                            ) {
                                Text(
                                    text = snip.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }

                        // Clipboard History Chips
                        clipboardHistory.take(4).forEach { historyItem ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF181822),
                                modifier = Modifier.clickable {
                                    onContentChange(content + historyItem)
                                }
                            ) {
                                Text(
                                    text = "📋 ${historyItem.replace('\n', ' ').take(18)}…",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showBgDialog) {
        CustomBackgroundDialog(
            currentUri = customBgUri,
            currentOpacity = bgOpacity,
            onSaveBackground = { uri, op ->
                onSetCustomBackground(uri, op)
                showBgDialog = false
            },
            onDismiss = { showBgDialog = false }
        )
    }
}
