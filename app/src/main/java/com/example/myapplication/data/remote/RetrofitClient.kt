package com.example.myapplication.data.remote


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.net.InetAddress
import java.net.NetworkInterface



object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.212:8000/"

    // Tạo interceptor để log request/response private
    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS) // Chờ kết nối 60s
        .writeTimeout(30, TimeUnit.SECONDS)   // Chờ gửi dữ liệu 60s
        .readTimeout(30, TimeUnit.SECONDS)    // Chờ nhận dữ liệu 60s
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
