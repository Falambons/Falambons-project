package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val name: String,
    val parentFolderId: Long? = null, // null for root folders
    val colorHex: String = "#00E5FF", // custom folder color
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
