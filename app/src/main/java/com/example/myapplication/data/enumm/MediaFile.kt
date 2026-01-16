package com.example.myapplication.data.enumm

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class MediaFile(
    val id: Long,
    val name: String,
    val duration: Long,
    val size: Long,
    val dateAdded: Long,
    val uri: Uri,
    val format: FormatType?= FormatType.MP3,
    val editType: EditType?= EditType.NOTHING,
    val mediaType: MediaType?= MediaType.AUDIO,
    var selectionOrder: Int = 0
) : Parcelable{
    override fun equals(other: Any?): Boolean {
        return other is MediaFile && other.id == id
    }

    override fun hashCode(): Int = id.hashCode()
}