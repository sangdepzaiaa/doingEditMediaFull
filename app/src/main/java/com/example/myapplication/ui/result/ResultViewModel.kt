package com.example.myapplication.ui.result

import android.util.Log
import com.example.myapplication.base.BaseViewModel
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.repository.MediaFileRepository
import com.example.myapplication.utils.toMediaEntry

private const val TAG = "ResultViewModel"

class ResultViewModel(
    private val mediaFileRepository: MediaFileRepository,
) : BaseViewModel() {

    suspend fun saveAudioReturnMediaFile(mediaFile: MediaFile): MediaFile {
        val mediaEntity = mediaFile.toMediaEntry()
        val id = mediaFileRepository.addMedia(mediaEntity)
        return mediaFile.copy(id = id)
    }

    suspend fun saveList(mediaFiles: List<MediaFile>): List<MediaFile> {
        val savedMediaFiles = mutableListOf<MediaFile>()

        for (mediaFile in mediaFiles) {
            try {
                val mediaFile = saveAudioReturnMediaFile(mediaFile)
                savedMediaFiles.add(mediaFile)
                Log.d(TAG, "Saved file with ID: ${mediaFile.id} - ${mediaFile.name}")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving file: ${mediaFile.name}", e)
            }
        }

        Log.d(TAG, "saveList completed: ${savedMediaFiles.size}/${mediaFiles.size} files saved")
        return savedMediaFiles
    }

    suspend fun updateNameMediaFile(id: Long, newName: String) {
        try {
            val rowCount = mediaFileRepository.updateNameMediaById(id, newName)
            if (rowCount > 0) {
                Log.d(TAG, "Media file updated: $newName")
            } else {
                Log.w(TAG, "No rows updated for media file: $newName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "updateMediaFile: ${e.message}")
            throw e
        }
    }

    suspend fun deleteMediaFile(id: Long) {
        try {
            val rowCount = mediaFileRepository.deleteMediaById(id)
            if (rowCount > 0) Log.d(TAG, "Media file deleted: $id")
            else {
                Log.w(TAG, "No rows deleted for media file: $id")
            }
        } catch (e: Exception) {
            Log.e(TAG, "deleteMediaFile: ${e.message}")
            throw e
        }
    }
}