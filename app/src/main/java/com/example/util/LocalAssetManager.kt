package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

object LocalAssetManager {

    /**
     * Copies a selected photo/GIF to internal private storage (/data/data/.../files/)
     * so it persists permanently even if the user restarts their phone,
     * closes the app, or Android revokes temporary picker permissions.
     */
    fun persistMediaLocally(
        context: Context,
        sourceUriStr: String?,
        subDirName: String,
        fileNamePrefix: String
    ): String? {
        if (sourceUriStr.isNullOrBlank()) return null

        val filesDir = context.filesDir
        // If already an internal private file, keep it
        if (sourceUriStr.startsWith(filesDir.absolutePath) || sourceUriStr.startsWith("file://${filesDir.absolutePath}")) {
            return sourceUriStr
        }

        return try {
            val uri = Uri.parse(sourceUriStr)
            val extension = when {
                sourceUriStr.contains(".gif", ignoreCase = true) -> "gif"
                sourceUriStr.contains(".png", ignoreCase = true) -> "png"
                sourceUriStr.contains(".webp", ignoreCase = true) -> "webp"
                else -> {
                    val mime = try { context.contentResolver.getType(uri) } catch (_: Exception) { null }
                    if (mime?.contains("gif", ignoreCase = true) == true) "gif"
                    else if (mime?.contains("png", ignoreCase = true) == true) "png"
                    else "jpg"
                }
            }

            val targetDir = File(filesDir, subDirName).apply { mkdirs() }
            val targetFile = File(targetDir, "${fileNamePrefix}_${System.currentTimeMillis()}.$extension")

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            if (targetFile.exists() && targetFile.length() > 0) {
                targetFile.absolutePath
            } else {
                sourceUriStr
            }
        } catch (e: Exception) {
            sourceUriStr
        }
    }
}
