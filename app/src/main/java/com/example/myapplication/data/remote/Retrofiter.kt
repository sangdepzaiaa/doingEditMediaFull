package com.example.myapplication.data.remote

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Retrofiter {

    val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.1.46:8080/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)
}