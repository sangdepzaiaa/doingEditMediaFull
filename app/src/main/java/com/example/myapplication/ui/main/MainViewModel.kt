package com.example.myapplication.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.repository.MediaFileRepository
import com.example.myapplication.data.local.dao.history.toMediaFile
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MediaFileRepository
) : ViewModel() {

    companion object {
        private const val MAX_FILES = 10
    }

    private val _allMedia = MutableLiveData<List<MediaFile>>()
    val allMedia: LiveData<List<MediaFile>> = _allMedia



    fun loadAllMedia() {
        viewModelScope.launch {
            try {
                val mediaEntities = repository.getAllMediaLimit(MAX_FILES)
                val mediaFiles =
                    mediaEntities.map { it.toMediaFile() }
                _allMedia.postValue(mediaFiles)
            } catch (e: Exception) {

                _allMedia.postValue(emptyList())
            }
        }
    }
}