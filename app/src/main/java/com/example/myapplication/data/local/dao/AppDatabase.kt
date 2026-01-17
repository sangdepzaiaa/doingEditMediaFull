package com.example.myapplication.data.local.dao

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.dao.history.MediaEntity
import com.example.myapplication.data.model.ImageEntity
import com.example.myapplication.utils.Converter
import java.util.concurrent.Executors

@Database(entities = [ImageEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile private var instance: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java, "app_db"
                ).setQueryCallback({
                                   sqlQuery, bindArgs ->
                    Log.d("RoomQuery", "SQL: $sqlQuery, Args: $bindArgs") },
                    Executors.newSingleThreadExecutor())
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}

@Database(
    entities = [MediaEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converter::class)
abstract class MyDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao

}