package com.example.myapplication.data.local.dao.history

import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType

@Entity(tableName = "media_files")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val path: String,
    val format: FormatType?,
    val mediaType: MediaType?,
    val editType: EditType?,
    val isHistory: Boolean = false
)

fun MediaEntity.toMediaFile(): MediaFile {
    return MediaFile(
        id = id,
        name = name,
        duration = duration,
        size = size,
        dateAdded = dateAdded,
        uri = path.toUri(),
        format = format,
        editType = editType,
        mediaType = mediaType,
        selectionOrder = 0
    )
}