package com.example.myapplication.data.repository

import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.local.dao.history.MediaEntity
import java.io.File

interface MediaFileRepository {
    suspend fun addMedia(media: MediaEntity): Long

    suspend fun addAll(mediaList: List<MediaEntity>)

    suspend fun updateMedia(media: MediaEntity): Int

    suspend fun deleteMedia(media: MediaEntity): Int

    suspend fun getAllMedia(): List<MediaEntity>

    suspend fun getMediaById(id: Long): MediaEntity?

    suspend fun getMediaByType(type: MediaType): List<MediaEntity>

    suspend fun getMediaByEditType(editType: EditType): List<MediaEntity>

    suspend fun getHistory(): List<MediaEntity>

    suspend fun updateNameMediaById(id: Long, name: String): Int

    suspend fun deleteMediaById(id: Long): Int

    suspend fun insertMediaFile(file: File, duration: Long, editType: EditType)

    suspend fun getAllMediaLimit(limit: Int): List<MediaEntity>
}