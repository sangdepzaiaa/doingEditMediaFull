package com.example.myapplication.ui.selectaudio.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.enumm.SortCriteria
import com.example.myapplication.data.enumm.SortOrder
import com.example.myapplication.data.model.MediaFolder
import com.example.myapplication.data.repository.MediaRepository
import kotlinx.coroutines.launch

class MediaViewModel(
    private val mediaType: MediaType,
    private val repository: MediaRepository
) : ViewModel() {
    private val _allFiles = MutableLiveData<List<MediaFile>>()
    val allFiles: LiveData<List<MediaFile>> = _allFiles
    private val _folders = MutableLiveData<List<MediaFolder>>()
    val folders: LiveData<List<MediaFolder>> = _folders
    private val _filesInFolder = MutableLiveData<List<MediaFile>>()
    val filesInFolder: LiveData<List<MediaFile>> = _filesInFolder
    private val _uiState = MutableLiveData<MediaScreenState>()
    val uiState: LiveData<MediaScreenState> = _uiState
    private val _sortCriteria = MutableLiveData(SortCriteria.DATE)
    val sortCriteria: LiveData<SortCriteria> = _sortCriteria

    private val _sortOrder = MutableLiveData(SortOrder.DESCENDING)
    val sortOrder: LiveData<SortOrder> = _sortOrder

    init {
        // Set the initial state
        _uiState.value = MediaScreenState.AllFiles
    }

    fun onTabSelected(position: Int) {
        val currentState = _uiState.value
        when (position) {
            0 -> if (currentState !is MediaScreenState.AllFiles) {
                _uiState.value = MediaScreenState.AllFiles
            }
            1 -> if (currentState !is MediaScreenState.Folders) {
                _uiState.value = MediaScreenState.Folders
            }
        }
    }

    fun onBackPressed() {
        // When back is pressed from FilesInFolder, go back to the Folders state
        if (_uiState.value is MediaScreenState.FilesInFolder) {
            _uiState.value = MediaScreenState.Folders
        }
    }

    private fun resortAllFileLists() {
        val criteria = _sortCriteria.value ?: return
        val order = _sortOrder.value ?: return

        _allFiles.value?.let { currentFiles ->
            _allFiles.value = sortFiles(currentFiles, criteria, order)
        }

        _filesInFolder.value?.let { currentFiles ->
            _filesInFolder.value = sortFiles(currentFiles, criteria, order)
        }
    }

    fun applySort(newCriteria: SortCriteria, newOrder: SortOrder) {
        _sortCriteria.value = newCriteria
        _sortOrder.value = newOrder
        resortAllFileLists()
    }

    private fun sortFiles(
        files: List<MediaFile>,
        criteria: SortCriteria,
        order: SortOrder
    ): List<MediaFile> {
        val sortedList = when (criteria) {
            SortCriteria.NAME -> files.sortedBy { it.name.lowercase() }
            SortCriteria.DURATION -> files.sortedBy { it.duration }
            SortCriteria.DATE -> files.sortedBy { it.dateAdded }
        }
        return if (order == SortOrder.DESCENDING) sortedList.reversed() else sortedList
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val criteria = _sortCriteria.value ?: SortCriteria.DATE
            val order = _sortOrder.value ?: SortOrder.DESCENDING
            val fetchedFiles = repository.getFiles(mediaType)
            _allFiles.postValue(sortFiles(fetchedFiles, criteria, order))
            _folders.postValue(repository.getFolders(mediaType))
        }
    }

    fun onFolderClicked(folder: MediaFolder) {
        _uiState.value = MediaScreenState.FilesInFolder(folder.id)
        viewModelScope.launch {
            val criteria = _sortCriteria.value ?: SortCriteria.DATE
            val order = _sortOrder.value ?: SortOrder.DESCENDING
            val fetchedFiles = repository.getFilesForFolder(mediaType, folder.id)
            _filesInFolder.postValue(sortFiles(fetchedFiles, criteria, order))
        }
    }
}

sealed class MediaScreenState {
    object AllFiles : MediaScreenState()
    object Folders : MediaScreenState()
    data class FilesInFolder(val folderId: String) : MediaScreenState()
}