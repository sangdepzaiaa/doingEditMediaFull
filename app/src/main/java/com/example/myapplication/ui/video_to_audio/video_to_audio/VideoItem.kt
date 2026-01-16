package com.example.myapplication.ui.video_to_audio.video_to_audio

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File

@Parcelize
data class VideoItem(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val size: Long,
    val path: String,
    val isSelected: Boolean = false,
    var selectionOrder: Int = 0

) : Parcelable {
    fun getDurationFormatted(): String {
        val seconds = (duration / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    fun getSizeFormatted(): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return if (mb >= 1) String.format("%.1f MB", mb) else String.format("%.0f KB", kb)
    }

    fun getFileType(): String {
        return if (path.isNotEmpty()) File(path).extension.uppercase() else "UNKNOWN"
    }

    fun withSelected(selected: Boolean): VideoItem = copy(isSelected = selected)

    // equality by uri string helper (optional)
    fun sameUriAs(other: VideoItem) = uri.toString() == other.uri.toString()
}