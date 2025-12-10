package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences

object SharePreUtils{
    private val PREFS_APP = "prefs_app"
    private var sharePrefs : SharedPreferences?=null

    fun prefs(context: Context) : SharedPreferences =
        sharePrefs ?: context.applicationContext.getSharedPreferences(
            PREFS_APP, Context.MODE_PRIVATE
        ).also { sharePrefs = it }


    fun getBoolean(context: Context,key:String = "",value:Boolean = false): Boolean =
        prefs(context).getBoolean(key,value)

    fun setBoolean(context: Context,key: String= "", value: Boolean = false) =
        prefs(context).edit().putBoolean(key,value).apply()

}

// also nhận 1 đối tượng SharedPreferences là it , gán vào sharePrefs
// hàm prefs trả về 1 đối tượng SharedPreferences, nên gọi được getBoolean và edit(),
// có edit( gọi được putBoolean và apply())
//applicationContext khi bạn cần context “toàn cục”, không gắn với UI.
//SharedPreferences
//Database
//WorkManager
//Service
//Dependency Injection (Koin, Hilt)
//Tải resource không liên quan UI
//KHÔNG dùng applicationContext cho:
//LayoutInflater
//Hiện Dialog
//Toast (vẫn được nhưng hạn chế)
//Context cần theme của Activity
//Resource phụ thuộc theme (R.attr.*, style, dynamic color)
// get lấy ra, set lưu vào
//set có ghi đè trùng key
//➤ get…()
//Không tạo file
//Không ghi gì
//Không tạo key
//Chỉ trả về default nếu key không có
//➤ set…()
//Tạo file SharedPreferences nếu chưa có
//Tạo key nếu chưa có
//Ghi đè (overwrite) nếu key có rồi