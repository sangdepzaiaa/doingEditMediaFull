package com.example.myapplication.data.remote


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.212:8000/"

    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Chờ kết nối 30s
        .writeTimeout(30, TimeUnit.SECONDS)   // Chờ gửi dữ liệu 30s
        .readTimeout(30, TimeUnit.SECONDS)    // Chờ nhận dữ liệu 30s
        .build()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Thêm client này vào
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
