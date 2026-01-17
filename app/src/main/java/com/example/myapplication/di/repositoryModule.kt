package com.example.myapplication.di

import com.example.myapplication.data.local.dao.history.MediaFileRepository
import com.example.myapplication.data.repositorylmpl.MediaFileRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    //single<MediaRepository> { MediaRepositoryImpl(get()) }
    single<MediaFileRepository> { MediaFileRepositoryImpl(get()) }

}