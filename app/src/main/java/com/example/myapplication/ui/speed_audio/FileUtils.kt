package com.example.myapplication.ui.speed_audio

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object FileUtils {

    /**
     * Lấy path từ URI, an toàn trên background thread
     */
    suspend fun getPath(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        when (uri.scheme?.lowercase()) {
            "file" -> uri.path
            "content" -> getContentPath(context, uri) ?: copyUriToCache(context, uri)
            else -> null
        }
    }

    /**
     * Query MediaStore để lấy path (chỉ cho các content URI local)
     */
    private fun getContentPath(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        return try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (index != -1) cursor.getString(index) else null
                } else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copy URI sang cache nếu không lấy được path trực tiếp
     */
    private fun copyUriToCache(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val fileName = uri.lastPathSegment?.takeIf { it.isNotBlank() } ?: "temp_file_${System.currentTimeMillis()}"
                val tempFile = File(context.cacheDir, fileName)
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
                tempFile.absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}