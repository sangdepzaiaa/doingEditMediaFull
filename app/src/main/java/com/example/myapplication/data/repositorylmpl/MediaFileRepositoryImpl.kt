package com.example.myapplication.data.repositorylmpl

import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.local.dao.MediaDao
import com.example.myapplication.data.local.dao.history.MediaEntity
import com.example.myapplication.data.local.dao.history.MediaFileRepository
import java.io.File

class MediaFileRepositoryImpl(
    private val dao: MediaDao
) : MediaFileRepository {

    override suspend fun addMedia(media: MediaEntity): Long {
        return dao.insertMedia(media)
    }

    override suspend fun addAll(mediaList: List<MediaEntity>) {
        dao.insertAllMedia(mediaList)
    }

    override suspend fun updateMedia(media: MediaEntity): Int {
        return dao.updateMedia(media)
    }

    override suspend fun deleteMedia(media: MediaEntity): Int {
        return dao.deleteMedia(media)
    }

    override suspend fun getAllMedia(): List<MediaEntity> {
        return dao.getAllMedia()
    }

    override suspend fun getMediaById(id: Long): MediaEntity? {
        return dao.getMediaById(id)
    }

    override suspend fun getMediaByType(type: MediaType): List<MediaEntity> {
        return dao.getMediaByMediaType(type)
    }

    override suspend fun getMediaByEditType(editType: EditType): List<MediaEntity> {
        return dao.getMediaByEditType(editType)
    }

    override suspend fun getHistory(): List<MediaEntity> {
        return dao.getHistory()
    }

    override suspend fun updateNameMediaById(id: Long, name: String): Int {
        return dao.updateNameMediaById(id, name)
    }

    override suspend fun deleteMediaById(id: Long): Int {
        return dao.deleteMediaById(id)
    }

    override suspend fun getAllMediaLimit(limit: Int): List<MediaEntity> {
        return dao.getAllMediaLimit(limit)
    }

    override suspend fun insertMediaFile(file: File, duration: Long, editType: EditType) {
        val entity = MediaEntity(
            name = file.name,
            duration = duration,
            size = file.length(),
            dateAdded = System.currentTimeMillis(),
            path = file.absolutePath,
            format = null,              // có thể set khi convert xong
            mediaType = MediaType.AUDIO, // tùy bạn logic
            editType = editType,
            isHistory = true
        )
        dao.insertMedia(entity)
    }
}