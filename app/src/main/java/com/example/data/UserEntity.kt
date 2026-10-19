package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val displayName: String,
    val avatarUri: String? = null,
    val avatarPresetId: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)
