package com.example.myapplication.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.local.dao.history.MediaFileRepository
import com.example.myapplication.data.local.dao.history.toMediaFile
import kotlinx.coroutines.launch

class HistoryViewmodel(
    private val mediaFileRepository: MediaFileRepository
) : ViewModel() {

    private val _allHistory = MutableLiveData<List<MediaFile>>()
    val allHistory: LiveData<List<MediaFile>> = _allHistory

    // Map để quản lý các LiveData theo EditType
    private val historyLiveDataMap = mapOf(
        EditType.AUDIO_CUTTER to MutableLiveData<List<MediaFile>>(),
        EditType.VIDEO_TO_AUDIO to MutableLiveData<List<MediaFile>>(),
        EditType.AUDIO_MERGER to MutableLiveData<List<MediaFile>>(),
        EditType.AUDIO_MIXER to MutableLiveData<List<MediaFile>>(),
        EditType.AUDIO_CONVERTER to MutableLiveData<List<MediaFile>>(),
        EditType.AUDIO_VOLUME to MutableLiveData<List<MediaFile>>(),
        EditType.VOICE_CHANGE to MutableLiveData<List<MediaFile>>(),
        EditType.TEXT_TO_AUDIO to MutableLiveData<List<MediaFile>>(),
        EditType.AUDIO_SPEED to MutableLiveData<List<MediaFile>>()
    )

    // Public LiveData
    val audioCutter: LiveData<List<MediaFile>> =  historyLiveDataMap[EditType.AUDIO_CUTTER] ?: MutableLiveData(emptyList())
    val videoToAudio: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.VIDEO_TO_AUDIO] ?: MutableLiveData(emptyList())
    val audioMerge: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.AUDIO_MERGER] ?: MutableLiveData(emptyList())
    val audioMixer: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.AUDIO_MIXER] ?: MutableLiveData(emptyList())
    val audioConvert: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.AUDIO_CONVERTER] ?: MutableLiveData(emptyList())
    val audioVolume: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.AUDIO_VOLUME] ?: MutableLiveData(emptyList())
    val voiceChange: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.VOICE_CHANGE] ?: MutableLiveData(emptyList())
    val textToAudio: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.TEXT_TO_AUDIO] ?: MutableLiveData(emptyList())
    val audioSpeed: LiveData<List<MediaFile>> = historyLiveDataMap[EditType.AUDIO_SPEED] ?: MutableLiveData(emptyList())

    fun loadHistory() {
        viewModelScope.launch {
            val historyList = mediaFileRepository.getHistory()
                .map { it.toMediaFile() }
            _allHistory.postValue(historyList)

            // Update all LiveData in one loop
            val groupedHistory = historyList.groupBy { it.editType }
            historyLiveDataMap.forEach { (editType, liveData) ->
                liveData.postValue(groupedHistory[editType] ?: emptyList())
            }
        }
    }
}