package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import androidx.core.content.edit

object SharePreUtils {
    private const val PREFS = "PREFS"

    fun pref(context: Context): SharedPreferences{
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getBoolean(context: Context,key: String ="", value: Boolean = false): Boolean{
        return pref(context).getBoolean(key,value)
    }

    fun setBoolean(context: Context,key: String="",value: Boolean= false): Boolean{
        return pref(context).edit().putBoolean(key,value).commit()
    }


    fun forceRated(context: Context) {
        val pre = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        pre.edit(commit = true) {
            putBoolean("rated", true)
        }
    }

    fun isRated(context: Context): Boolean {
        val pre = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return pre.getBoolean("rated", false)
    }
}
// also nhận 1 đối tượng SharedPreferences là it , gán vào sharePrefs
//apply() KHÔNG trả về Boolean
//apply() trả về Unit
//commit() trả về Boolean
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