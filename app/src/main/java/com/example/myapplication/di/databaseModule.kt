package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.dao.AppDatabase
import com.example.myapplication.data.local.dao.MediaDao
import com.example.myapplication.data.local.dao.MyDatabase
import org.koin.dsl.module

val databaseModule = module {
    single { provideDatabase(get()) }
    single { provideMediaDao(get()) }


}

fun provideDatabase(context: Context): MyDatabase = Room.databaseBuilder(
    context.applicationContext,
    MyDatabase::class.java,
    "app_database"
).fallbackToDestructiveMigration().build()

fun provideMediaDao(myDatabase: MyDatabase): MediaDao = myDatabase.mediaDao()