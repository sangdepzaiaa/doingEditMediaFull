package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.local.dao.history.MediaEntity

@Dao
interface MediaDao {

    @Query("SELECT * FROM media_files WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(file: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(file: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity): Int

    @Delete
    suspend fun deleteMedia(file: MediaEntity): Int

    @Query("DELETE FROM media_files WHERE id = :id")
    suspend fun deleteMediaById(id: Long): Int

    @Query("SELECT * FROM media_files ORDER BY dateAdded DESC")
    suspend fun getAllMedia(): List<MediaEntity>

    @Query("SELECT * FROM media_files WHERE editType = :editType")
    suspend fun getMediaByEditType(editType: EditType): List<MediaEntity>

    @Query("SELECT * FROM media_files WHERE mediaType = :type")
    suspend fun getMediaByMediaType(type: MediaType): List<MediaEntity>

    @Query("SELECT * FROM media_files WHERE isHistory = 1 ORDER BY dateAdded DESC")
    suspend fun getHistory(): List<MediaEntity>

    @Query("UPDATE media_files SET name = :name WHERE id = :id")
    suspend fun updateNameMediaById(id: Long, name: String): Int

    @Query("SELECT * FROM media_files ORDER BY id DESC LIMIT :limit")
    suspend fun getAllMediaLimit(limit: Int): List<MediaEntity>
}