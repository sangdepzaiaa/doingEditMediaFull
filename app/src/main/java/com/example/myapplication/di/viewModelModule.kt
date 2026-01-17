package com.example.myapplication.di

import com.example.myapplication.data.local.dao.history.MediaFileRepository
import com.example.myapplication.ui.history.HistoryViewmodel
import com.example.myapplication.ui.home.HomeViewModel
import com.example.myapplication.ui.main.MainViewModel
import com.example.myapplication.ui.result.ResultViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { ResultViewModel(get<MediaFileRepository>()) }
    viewModel { HomeViewModel(get()) }
    viewModel { HistoryViewmodel(get()) }
    viewModel { MainViewModel(get()) }
}