package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.model.ImageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(images: List<ImageEntity>)

    @Query("DELETE FROM images WHERE id NOT IN (:ids)")
    suspend fun deleteMissing(ids: List<Int>)

    @Transaction
    suspend fun syncImages(images: List<ImageEntity>) {
        val uniqueImages = images.distinctBy { it.id }
        insertOrUpdate(uniqueImages)
        deleteMissing(uniqueImages.map { it.id })
    }

    @Query("SELECT * FROM images ORDER BY id DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Delete
    suspend fun delete(image: ImageEntity)
}
