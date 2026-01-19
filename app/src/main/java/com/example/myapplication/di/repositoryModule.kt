package com.example.myapplication.di

import com.example.myapplication.data.repository.MediaFileRepository
import com.example.myapplication.data.repository.MediaRepository
import com.example.myapplication.data.repositorylmpl.MediaFileRepositoryImpl
import com.example.myapplication.data.repositorylmpl.MediaRepositoryImpl
import org.koin.dsl.module

val repositoryModule = module {
    single<MediaRepository> { MediaRepositoryImpl(get()) }
    single<MediaFileRepository> { MediaFileRepositoryImpl(get()) }

}