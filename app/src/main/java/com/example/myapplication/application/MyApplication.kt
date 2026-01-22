package com.example.myapplication.application

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.myapplication.di.databaseModule
import com.example.myapplication.di.repositoryModule
import com.example.myapplication.di.viewModelModule
import com.example.myapplication.utils.SystemUtil
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


class MyApplication: Application(), KoinComponent {

    override fun onCreate() {
        super.onCreate()
        SystemUtil.applySavedLocale(this@MyApplication)

        val modules = listOf(
//            networkModule,
            viewModelModule,
            databaseModule,
            repositoryModule
        )
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(modules)
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}