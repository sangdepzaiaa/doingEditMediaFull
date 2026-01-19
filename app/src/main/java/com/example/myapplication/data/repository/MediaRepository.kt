package com.example.myapplication.data.repository

import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.model.MediaFolder

interface MediaRepository {
    suspend fun getFiles(mediaType: MediaType): List<MediaFile>
    suspend fun getFolders(mediaType: MediaType): List<MediaFolder>
    suspend fun getFilesForFolder(mediaType: MediaType, folderId: String): List<MediaFile>
}