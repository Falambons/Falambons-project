package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val folderId: Long? = null, // null for files in root
    val name: String,
    val extension: String = "txt",
    val content: String = "",
    val colorHex: String = "#38BDF8", // custom file color
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = if (name.endsWith(".$extension", ignoreCase = true)) name else "$name.$extension"
}
