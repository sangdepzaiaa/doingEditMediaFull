package com.example.myapplication.data.repositorylmpl

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.model.MediaFolder
import com.example.myapplication.data.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(private val context: Context) : MediaRepository {
    override suspend fun getFiles(mediaType: MediaType): List<MediaFile> {
        val files = mutableListOf<MediaFile>()

        withContext(Dispatchers.IO) {
            val collectionUri = when (mediaType) {
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE
            )

            val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
            // Get all supported MIME types from FormatType
            val supportedMimeTypes = getSupportedMimeTypes()
            val mimeTypePlaceholders = supportedMimeTypes.joinToString(",") { "?" }
            val selection = "${MediaStore.MediaColumns.MIME_TYPE} IN ($mimeTypePlaceholders)"
            val selectionArgs = supportedMimeTypes.toTypedArray()
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )?.use { cursor -> // 'use' ensures the cursor is closed automatically
                Log.d("AudioDebug", "Query successful. Found ${cursor.count} files.")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val contentUri = ContentUris.withAppendedId(collectionUri, id)

                    // Find FormatType by MIME type
                    val format = findFormatTypeByMimeType(mimeType)

                    Log.d("AudioDebug", "  - Found file: $name (MIME: $mimeType, Format: $format)")
                    files.add(MediaFile(id, name, duration, size, dateAdded, contentUri, format))
                }
            }
        }
        Log.d("AudioDebug", "Returning a list with ${files.size} files.")
        return files
    }

    override suspend fun getFolders(mediaType: MediaType): List<MediaFolder> {
        val foldersMap = mutableMapOf<String, MediaFolder>()

        withContext(Dispatchers.IO) {
            val collectionUri = when (mediaType) {
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.MediaColumns.BUCKET_ID,
                MediaStore.MediaColumns.BUCKET_DISPLAY_NAME
            )
            val supportedMimeTypes = getSupportedMimeTypes()
            val mimeTypePlaceholders = supportedMimeTypes.joinToString(",") { "?" }
            val selection = "${MediaStore.MediaColumns.MIME_TYPE} IN ($mimeTypePlaceholders)"
            val selectionArgs = supportedMimeTypes.toTypedArray()
            context.contentResolver.query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val bucketIdColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
                val bucketNameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)

                while (cursor.moveToNext()) {
                    val bucketId = cursor.getString(bucketIdColumn)
                    val bucketName = cursor.getString(bucketNameColumn) ?: "Unknown"

                    // If folder is already in map, increment its item count, otherwise add it
                    val folder = foldersMap[bucketId]
                    if (folder == null) {
                        foldersMap[bucketId] = MediaFolder(bucketId, bucketName, 1)
                    } else {
                        foldersMap[bucketId] = folder.copy(itemCount = folder.itemCount + 1)
                    }
                }
            }
        }
        return foldersMap.values.sortedBy { it.name }
    }

    override suspend fun getFilesForFolder(
        mediaType: MediaType,
        folderId: String
    ): List<MediaFile> {
        val files = mutableListOf<MediaFile>()
        Log.d("AudioDebug", "Starting query for files in folderId: $folderId")

        withContext(Dispatchers.IO) {
            val collectionUri = when (mediaType) {
                MediaType.AUDIO -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                MediaType.VIDEO -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DURATION,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DATE_ADDED,
                MediaStore.MediaColumns.MIME_TYPE
            )

            // filter by folder BUCKET_ID
            val supportedMimeTypes = getSupportedMimeTypes()
            val mimeTypePlaceholders = supportedMimeTypes.joinToString(",") { "?" }
            val selection =
                "${MediaStore.MediaColumns.BUCKET_ID} = ? AND ${MediaStore.MediaColumns.MIME_TYPE} IN ($mimeTypePlaceholders)"
            val selectionArgs = arrayOf(folderId) + supportedMimeTypes.toTypedArray()
            val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"

            context.contentResolver.query(
                collectionUri,
                projection,
                selection,      // filter
                selectionArgs,  // folderId
                sortOrder
            )?.use { cursor ->
                Log.d("AudioDebug", "Query for folder successful. Found ${cursor.count} files.")
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val dateAddedColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
                val mimeTypeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                // Find FormatType by MIME type

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val dateAdded = cursor.getLong(dateAddedColumn)
                    val contentUri = ContentUris.withAppendedId(collectionUri, id)
                    val mimeType = cursor.getString(mimeTypeColumn)
                    val format = findFormatTypeByMimeType(mimeType)

                    Log.d(
                        "AudioDebug",
                        "  - Found file on folder: $name (MIME: $mimeType, Format: $format)"
                    )
                    files.add(MediaFile(id, name, duration, size, dateAdded, contentUri, format))
                }
            }
        }
        Log.d("AudioDebug", "Returning a list with ${files.size} files for the folder.")
        return files
    }

    private fun getSupportedMimeTypes(): List<String> {
        return FormatType.entries.flatMap { formatType ->
            when (formatType) {
                FormatType.WAV -> listOf("audio/wav", "audio/x-wav", "audio/wave")
                FormatType.AAC -> listOf(
                    "audio/aac",
                    "audio/mp4",
                    "audio/x-aac",
                    "audio/mp4a-latm",
                    "audio/m4a",
                    "audio/aac-adts"
                )

                FormatType.MP3 -> listOf("audio/mpeg", "audio/mp3", "audio/x-mp3")
                FormatType.FLAC -> listOf("audio/flac", "audio/x-flac")
                FormatType.OGG -> listOf("audio/ogg", "audio/x-ogg")
            }
        }
    }

    private fun findFormatTypeByMimeType(mimeType: String): FormatType? {
        return FormatType.entries.find { formatType ->
            when (formatType) {
                FormatType.WAV -> mimeType in listOf("audio/wav", "audio/x-wav", "audio/wave")
                FormatType.AAC -> mimeType in listOf(
                    "audio/aac",
                    "audio/mp4",
                    "audio/mp4a-latm",
                    "audio/m4a",
                    "audio/aac-adts",
                    "audio/x-aac"
                )

                FormatType.MP3 -> mimeType in listOf("audio/mpeg", "audio/mp3", "audio/x-mp3")
                FormatType.FLAC -> mimeType in listOf("audio/flac", "audio/x-flac")
                FormatType.OGG -> mimeType in listOf("audio/ogg", "audio/x-ogg")
            }
        }
    }
}