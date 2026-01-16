package com.example.myapplication.ui.video_to_audio.video_to_audio

import android.app.Application
import android.content.ContentUris
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.data.model.VideoMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VideoToAudioViewModel(application: Application) : AndroidViewModel(application) {

    private val _allVideos = MutableLiveData<List<VideoItem>>(emptyList())
    val allVideos: LiveData<List<VideoItem>> = _allVideos

    private val _selectedVideos = MutableLiveData<List<VideoItem>>(emptyList())
    val selectedVideos: LiveData<List<VideoItem>> = _selectedVideos

    private val maxSelection = 4

    // ====== PUBLIC API ======

    fun setPickedVideos(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val videos = uris.mapNotNull { uri ->
                runCatching {
                    val info = queryVideoInfo(uri)
                    VideoItem(
                        uri = uri,
                        name = info.name,
                        duration = info.duration,
                        size = info.size,
                        path = "" // SAF không có file path
                    )
                }.getOrNull()
            }

            _allVideos.postValue(videos)
            _selectedVideos.postValue(emptyList()) // reset selection khi pick lại
        }
    }

    fun addSelected(video: VideoItem): Boolean {
        val current = _selectedVideos.value ?: emptyList()
        if (current.any { it.uri.toString() == video.uri.toString() }) return false
        if (current.size >= maxSelection) return false
        _selectedVideos.value = current + video
        return true
    }

    fun removeSelected(video: VideoItem) {
        val current = _selectedVideos.value ?: emptyList()
        _selectedVideos.value = current.filterNot { it.uri.toString() == video.uri.toString() }
    }

    fun setSelectedVideos(list: List<VideoItem>) {
        val unique = list.distinctBy { it.uri.toString() }.take(maxSelection)
        _selectedVideos.value = unique
    }

    fun clearSelection() {
        _selectedVideos.value = emptyList()
    }

    // ====== PRIVATE ======

    private fun queryVideoInfo(uri: Uri): VideoMeta {
        val cr = getApplication<Application>().contentResolver

        var name = "video"
        var size = 0L
        var duration = 0L

        // ---- File name & size ----
        cr.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = cursor.getString(nameIndex)
                }

                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    size = cursor.getLong(sizeIndex)
                }
            }
        }

        // ---- Duration (safe for Android 13–14) ----
        val retriever = MediaMetadataRetriever()
        try {
            cr.openFileDescriptor(uri, "r")?.use { fd ->
                retriever.setDataSource(fd.fileDescriptor)
                duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        ?.toLong() ?: 0
            }
        } catch (e: Exception) {
            duration = 0
        } finally {
            retriever.release()
        }

        return VideoMeta(name, size, duration)
    }
}
