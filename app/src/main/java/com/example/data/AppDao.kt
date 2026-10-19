package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- USER AUTH ---
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: Long): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    // --- FOLDERS ---
    @Query("SELECT * FROM folders WHERE userId = :userId AND parentFolderId IS NULL ORDER BY isFavorite DESC, name ASC")
    fun getRootFolders(userId: Long): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE userId = :userId AND parentFolderId = :parentId ORDER BY isFavorite DESC, name ASC")
    fun getSubFolders(userId: Long, parentId: Long): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE userId = :userId ORDER BY isFavorite DESC, name ASC")
    fun getAllFolders(userId: Long): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :folderId LIMIT 1")
    suspend fun getFolderById(folderId: Long): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity): Long

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolderById(folderId: Long)

    // --- FILES ---
    @Query("SELECT * FROM files WHERE userId = :userId AND folderId IS NULL ORDER BY isFavorite DESC, updatedAt DESC")
    fun getRootFiles(userId: Long): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE userId = :userId AND folderId = :folderId ORDER BY isFavorite DESC, updatedAt DESC")
    fun getFilesByFolder(userId: Long, folderId: Long): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE userId = :userId ORDER BY isFavorite DESC, updatedAt DESC")
    fun getAllFiles(userId: Long): Flow<List<FileEntity>>

    @Query("SELECT * FROM files WHERE id = :fileId LIMIT 1")
    suspend fun getFileById(fileId: Long): FileEntity?

    @Query("SELECT * FROM files WHERE id = :fileId LIMIT 1")
    fun observeFileById(fileId: Long): Flow<FileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: FileEntity): Long

    @Update
    suspend fun updateFile(file: FileEntity)

    @Delete
    suspend fun deleteFile(file: FileEntity)

    @Query("DELETE FROM files WHERE id = :fileId")
    suspend fun deleteFileById(fileId: Long)

    @Query("DELETE FROM files WHERE folderId = :folderId")
    suspend fun deleteFilesByFolderId(folderId: Long)
}
