package com.example.myapplication.di

import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.repository.MediaFileRepository
import com.example.myapplication.data.repository.MediaRepository
import com.example.myapplication.ui.history.HistoryViewmodel
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.main.MainViewModel
import com.example.myapplication.ui.result.ResultViewModel
import com.example.myapplication.ui.selectaudio.view_model.MediaViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ResultViewModel(get<MediaFileRepository>()) }
    viewModel { HomeViewModel(get()) }
    viewModel { HistoryViewmodel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { (mediaType: MediaType) -> MediaViewModel(mediaType, get<MediaRepository>()) }

}