package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    // --- AUTH ---
    suspend fun findUserByUsername(username: String): UserEntity? =
        appDao.getUserByUsername(username.trim())

    suspend fun findUserById(id: Long): UserEntity? =
        appDao.getUserById(id)

    fun observeUser(id: Long): Flow<UserEntity?> =
        appDao.observeUserById(id)

    suspend fun registerUser(
        username: String,
        passwordPlain: String,
        displayName: String,
        avatarUri: String? = null,
        avatarPresetId: Int = 1
    ): Long {
        val user = UserEntity(
            username = username.trim(),
            passwordHash = passwordPlain, // Local offline storage
            displayName = displayName.trim().ifEmpty { username.trim() },
            avatarUri = avatarUri,
            avatarPresetId = avatarPresetId
        )
        val userId = appDao.insertUser(user)
        seedInitialFilesForUser(userId)
        return userId
    }

    suspend fun updateUserProfile(user: UserEntity) {
        appDao.updateUser(user)
    }

    // --- FOLDERS ---
    fun getRootFolders(userId: Long): Flow<List<FolderEntity>> =
        appDao.getRootFolders(userId)

    fun getSubFolders(userId: Long, parentId: Long): Flow<List<FolderEntity>> =
        appDao.getSubFolders(userId, parentId)

    suspend fun getFolderById(id: Long): FolderEntity? =
        appDao.getFolderById(id)

    suspend fun createFolder(
        userId: Long,
        name: String,
        parentId: Long? = null,
        colorHex: String = "#00E5FF"
    ): Long {
        val folder = FolderEntity(
            userId = userId,
            name = name.trim(),
            parentFolderId = parentId,
            colorHex = colorHex
        )
        return appDao.insertFolder(folder)
    }

    suspend fun updateFolder(folder: FolderEntity) {
        appDao.updateFolder(folder)
    }

    suspend fun deleteFolder(folderId: Long) {
        appDao.deleteFilesByFolderId(folderId)
        appDao.deleteFolderById(folderId)
    }

    // --- FILES ---
    fun getRootFiles(userId: Long): Flow<List<FileEntity>> =
        appDao.getRootFiles(userId)

    fun getFilesByFolder(userId: Long, folderId: Long): Flow<List<FileEntity>> =
        appDao.getFilesByFolder(userId, folderId)

    fun getAllFiles(userId: Long): Flow<List<FileEntity>> =
        appDao.getAllFiles(userId)

    suspend fun getFileById(fileId: Long): FileEntity? =
        appDao.getFileById(fileId)

    fun observeFileById(fileId: Long): Flow<FileEntity?> =
        appDao.observeFileById(fileId)

    suspend fun createFile(
        userId: Long,
        folderId: Long? = null,
        name: String,
        extension: String,
        content: String = "",
        colorHex: String = "#38BDF8"
    ): Long {
        val cleanName = name.trim().removeSuffix(".$extension")
        val file = FileEntity(
            userId = userId,
            folderId = folderId,
            name = cleanName,
            extension = extension.lowercase().trim().removePrefix("."),
            content = content,
            colorHex = colorHex,
            updatedAt = System.currentTimeMillis()
        )
        return appDao.insertFile(file)
    }

    suspend fun updateFile(file: FileEntity) {
        appDao.updateFile(file.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteFile(fileId: Long) {
        appDao.deleteFileById(fileId)
    }

    // --- INITIAL SEED FOR NEW OFFLINE USER ---
    private suspend fun seedInitialFilesForUser(userId: Long) {
        // Create sample project folders
        val webFolderId = appDao.insertFolder(
            FolderEntity(
                userId = userId,
                name = "Web_Projects",
                colorHex = "#00E5FF",
                isFavorite = true
            )
        )

        val backendFolderId = appDao.insertFolder(
            FolderEntity(
                userId = userId,
                name = "Backend_Scripts",
                colorHex = "#A855F7",
                isFavorite = false
            )
        )

        val nativeFolderId = appDao.insertFolder(
            FolderEntity(
                userId = userId,
                name = "Cpp_Algorithms",
                colorHex = "#22C55E",
                isFavorite = true
            )
        )

        // Seed Web Files
        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = webFolderId,
                name = "index",
                extension = "html",
                colorHex = "#E34F26",
                content = """<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Falambons Code Studio</title>
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <div class="card">
        <h1>Falambons Developer</h1>
        <p>Aplikasi Text & Code Editor Cepat dan Ringan.</p>
        <button id="btnAction">Mulai Coding</button>
    </div>
    <script src="app.js"></script>
</body>
</html>"""
            )
        )

        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = webFolderId,
                name = "style",
                extension = "css",
                colorHex = "#2965F1",
                content = """/* Falambons Dark Styling */
body {
    background-color: #0A0A10;
    color: #FFFFFF;
    font-family: 'JetBrains Mono', monospace;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    margin: 0;
}

.card {
    background: linear-gradient(135deg, #13141F 0%, #1A1C2E 100%);
    border: 1px solid #00E5FF;
    border-radius: 16px;
    padding: 24px;
    box-shadow: 0 0 20px rgba(0, 229, 255, 0.25);
    text-align: center;
}

button {
    background: #00E5FF;
    color: #000000;
    font-weight: bold;
    border: none;
    padding: 12px 24px;
    border-radius: 8px;
    cursor: pointer;
    transition: 0.3s ease;
}"""
            )
        )

        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = webFolderId,
                name = "app",
                extension = "js",
                colorHex = "#F7DF1E",
                content = """// Falambons Code Engine
console.log("Falambons Developer Code Studio Loaded!");

const btn = document.getElementById("btnAction");
btn.addEventListener("click", () => {
    alert("Selamat datang di Falambons Code!");
    console.log("Ready to compile and share via QR Code!");
});"""
            )
        )

        // Seed C++ File
        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = nativeFolderId,
                name = "main",
                extension = "cpp",
                colorHex = "#00599C",
                isFavorite = true,
                content = """#include <iostream>
#include <string>
#include <vector>

// Falambons C++ Engine
int main() {
    std::string creator = "Falambons Developer";
    std::cout << "=== Falambons Code Studio ===" << std::endl;
    std::cout << "Engine C++ Fast Runner Active!" << std::endl;
    std::cout << "Credit by: " << creator << std::endl;

    for (int i = 1; i <= 5; ++i) {
        std::cout << "Loop step: " << i << std::endl;
    }

    return 0;
}"""
            )
        )

        // Seed PHP File
        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = backendFolderId,
                name = "api_server",
                extension = "php",
                colorHex = "#8892BF",
                content = """<?php
// Falambons Backend API Server
header('Content-Type: application/json; charset=utf-8');

${'$'}response = [
    'status' => 'success',
    'app' => 'Falambons Code Studio',
    'developer' => 'falambons developer',
    'offline_mode' => true,
    'timestamp' => time()
];

echo json_encode(${'$'}response, JSON_PRETTY_PRINT);
?>"""
            )
        )

        // Seed Root File (txt)
        appDao.insertFile(
            FileEntity(
                userId = userId,
                folderId = null,
                name = "catatan_falambons",
                extension = "txt",
                colorHex = "#F59E0B",
                isFavorite = true,
                content = """=== FALAMBONS CODE STUDIO ===
Credit by : falambons developer

Fitur Unggulan:
1. Latar belakang hitam dengan 3 Tema Global:
   - Pure Dark (Amoled)
   - Blue Dark (Cyberpunk)
   - RGB Theme (Neon Glowing)
2. Unlimited baris panjang (Horizontal Scroll & Word Wrap toggle)
3. Fast Coding toolbar: Quick brackets & snippet JS, CSS, C++, PHP, HTML
4. Mode auto copy-paste text & clipboard buffer
5. Custom background image & GIF dengan opacity slider
6. Auto-save otomatis saat mengetik dan saat keluar aplikasi
7. QR Code / Barcode generator custom warna dan logo
8. Export PDF rapi dengan nomor baris
9. Floating Code Pad: overlay melayang di atas layar
10. Login & Register offline aman di local storage Android"""
            )
        )
    }
}
