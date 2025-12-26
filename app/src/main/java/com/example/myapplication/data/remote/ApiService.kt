package com.example.myapplication.data.remote

import com.example.myapplication.data.model.Image
import com.example.myapplication.data.model.Note
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @GET("/note")
    fun getAllNote() : Call<List<Note>>


}