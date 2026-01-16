package com.example.myapplication.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import kotlin.coroutines.resume

object MediaScanner {
    private const val TAG = "MediaScanner"

    suspend fun scanFile(
        context: Context,
        tempFileFromFfmpeg: File,
        editType: EditType,
        newFileName: String,
        newFileFormat: FormatType
    ): Pair<MediaFile, File>? = withContext(Dispatchers.IO) {
        val contentResolver: ContentResolver = context.contentResolver
        var mediaStoreUri: Uri? = null // Declare here for potential cleanup

        try {
            // Add file checks first
            if (!tempFileFromFfmpeg.exists()) {
                Log.e(TAG, "FFmpeg output file does not exist: ${tempFileFromFfmpeg.absolutePath}")
                return@withContext null
            }

            if (!tempFileFromFfmpeg.canRead()) {
                Log.e(TAG, "FFmpeg output file is not readable: ${tempFileFromFfmpeg.absolutePath}")
                return@withContext null
            }

            val currentTimestamp = System.currentTimeMillis()
            val fullDisplayName = newFileName + newFileFormat.extension
            val relativePath = getRelativePath(newFileFormat)

            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fullDisplayName)
                put(MediaStore.MediaColumns.MIME_TYPE, newFileFormat.mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.DATE_ADDED, currentTimestamp / 1000)
                put(MediaStore.MediaColumns.DATE_MODIFIED, currentTimestamp / 1000)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }

            val collectionUri = when {
                newFileFormat.isAudio() -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            mediaStoreUri = contentResolver.insert(collectionUri, values)

            if (mediaStoreUri == null) {
                Log.e(TAG, "Failed to insert new media into MediaStore for: $fullDisplayName")
                return@withContext null
            }

            contentResolver.openOutputStream(mediaStoreUri)?.use { outputStream ->
                FileInputStream(tempFileFromFfmpeg).use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: run {
                Log.e(TAG, "Failed to open output stream for MediaStore URI: $mediaStoreUri")
                try {
                    contentResolver.delete(mediaStoreUri, null, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting failed MediaStore entry (stream fail)", e)
                }
                return@withContext null
            }

            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            values.put(MediaStore.MediaColumns.SIZE, tempFileFromFfmpeg.length())
            contentResolver.update(mediaStoreUri, values, null, null)

            tempFileFromFfmpeg.delete()
            Log.d(TAG, "Deleted temporary FFmpeg file: ${tempFileFromFfmpeg.absolutePath}")

            var finalPublicFileAbsolutePath: String? = null
            contentResolver.query(
                mediaStoreUri,
                arrayOf(MediaStore.MediaColumns.DATA),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val dataColumnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                    if (dataColumnIndex != -1) {
                        finalPublicFileAbsolutePath = cursor.getString(dataColumnIndex)
                    }
                }
            }

            if (finalPublicFileAbsolutePath == null) {
                finalPublicFileAbsolutePath = File(
                    Environment.getExternalStorageDirectory(),
                    relativePath + File.separator + fullDisplayName
                ).absolutePath
                Log.w(
                    TAG,
                    "MediaStore.MediaColumns.DATA was null. Reconstructing absolute path as fallback: $finalPublicFileAbsolutePath"
                )
            }

            val scannedUriFromConnection = suspendCancellableCoroutine { continuation ->
                if (finalPublicFileAbsolutePath != null) {
                    MediaScannerConnection.scanFile(
                        context,
                        arrayOf(finalPublicFileAbsolutePath),
                        arrayOf(newFileFormat.mimeType)
                    ) { path, uri ->
                        Log.d(TAG, "MediaScanner finished: Path=$path, URI=$uri")
                        continuation.resume(uri)
                    }
                } else {
                    Log.e(TAG, "Failed to determine absolute path for MediaScannerConnection.")
                    continuation.resume(null)
                }
            }

            if (scannedUriFromConnection == null) {
                Log.e(
                    TAG,
                    "MediaScannerConnection failed to return a valid URI after scan for path: $finalPublicFileAbsolutePath"
                )
                try {
                    contentResolver.delete(mediaStoreUri, null, null)
                } catch (e: Exception) {
                    Log.e(TAG, "Error deleting orphaned MediaStore entry", e)
                }
                return@withContext null
            }

            var newMediaFile: MediaFile?
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.DATA
            )

            contentResolver.query(scannedUriFromConnection, projection, null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                        val name =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                        val duration =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION))
                        val size =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
                        val dateAdded =
                            cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
                        val actualContentUri = ContentUris.withAppendedId(collectionUri, id)

                        val finalDbAbsolutePath: String? =
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))

                        newMediaFile = MediaFile(
                            id = id,
                            name = name,
                            duration = duration,
                            size = size,
                            dateAdded = dateAdded * 1000,
                            uri = actualContentUri,
                            format = newFileFormat,
                            editType = editType,
                            mediaType = mediaTypeFromFormatType(),
                            selectionOrder = 0
                        )

                        val finalFileForDb = File(
                            finalDbAbsolutePath ?: finalPublicFileAbsolutePath!!
                        )

                        Log.d(
                            TAG,
                            "Scanned success. New MediaFile: ${newMediaFile.name}, URI: ${newMediaFile.uri}, Path: ${finalFileForDb.absolutePath} format: ${newMediaFile.format}"
                        )
                        return@withContext newMediaFile to finalFileForDb
                    }
                }
            Log.e(
                TAG,
                "Failed to construct MediaFile object after successful query for: $scannedUriFromConnection"
            )
            try {
                contentResolver.delete(scannedUriFromConnection, null, null)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting orphaned MediaStore entry", e)
            }
            return@withContext null

        } catch (e: Exception) {
            Log.e(TAG, "Critical error during file scan/save: ${e.message}", e)
            if (mediaStoreUri != null) {
                try {
                    contentResolver.delete(mediaStoreUri, null, null)
                } catch (deleteEx: Exception) {
                    Log.e(TAG, "Error deleting MediaStore entry after critical exception", deleteEx)
                }
            }
            return@withContext null
        }
    }

    private fun getAppSpecificDirectory(
        context: Context,
        formatType: FormatType
    ): File {
        return File(
            when {
                formatType.isAudio() -> Environment.DIRECTORY_MUSIC
                else -> Environment.DIRECTORY_DOWNLOADS
            },
            "AudioCutterApp"
        )
    }

    private fun getRelativePath(formatType: FormatType): String {
        return (when {
            formatType.isAudio() -> Environment.DIRECTORY_MUSIC
            else -> Environment.DIRECTORY_DOWNLOADS
        }) + File.separator + "AudioCutterApp"
    }

    private fun FormatType.isAudio(): Boolean {
        return this.mimeType.startsWith("audio/")
    }

    private fun getMediaDuration(context: Context, uri: Uri): Long {
        try {
            context.contentResolver.query(
                uri,
                arrayOf(MediaStore.MediaColumns.DURATION),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val durationColumn = cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)
                    if (durationColumn != -1) {
                        return cursor.getLong(durationColumn)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting media duration from URI: $uri", e)
        }
        return 0L
    }

    private fun mediaTypeFromFormatType(): MediaType {
        return MediaType.AUDIO
    }
}