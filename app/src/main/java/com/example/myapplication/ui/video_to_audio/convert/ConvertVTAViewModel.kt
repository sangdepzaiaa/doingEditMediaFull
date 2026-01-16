package com.example.myapplication.ui.video_to_audio.convert

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.ui.video_to_audio.video_to_audio.VideoItem

class ConvertVTAViewModel(application: Application) : AndroidViewModel(application) {

    private val _selectedVideos = MutableLiveData<List<VideoItem>>(emptyList())
    val selectedVideos: LiveData<List<VideoItem>> = _selectedVideos

    private val _selectedFormat = MutableLiveData<FormatType>(FormatType.MP3)
    val selectedFormat: LiveData<FormatType> = _selectedFormat

    fun setVideos(videos: List<VideoItem>) {
        // ensure unique by uri
        _selectedVideos.value = videos.distinctBy { it.uri.toString() }
    }

    fun removeVideo(video: VideoItem) {
        _selectedVideos.value = (_selectedVideos.value ?: emptyList()).filterNot { it.uri.toString() == video.uri.toString() }
    }

    fun removeVideoAt(position: Int) {
        val list = (_selectedVideos.value ?: emptyList()).toMutableList()
        if (position in list.indices) {
            list.removeAt(position)
            _selectedVideos.value = list.toList()
        }
    }

    fun selectFormat(format: FormatType) {
        _selectedFormat.value = format
    }
}