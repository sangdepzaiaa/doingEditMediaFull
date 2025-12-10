package com.example.myapplication.utils

import android.content.Context
import android.net.Uri
import android.os.SystemClock
import android.view.View
import java.io.File
import java.io.FileOutputStream

fun View.tap(interval: Long = 1000L, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) return@setOnClickListener
        lastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }
}

fun Uri.copyToCacheFile(context: Context, filename: String): File? {
    return try {
        val file = File(context.cacheDir, filename)
        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//Copy dữ liệu từ URI sang file
//context.contentResolver :  làm việc với URI (content://...)
// có thể:
//mở file từ Gallery
//đọc ảnh từ MediaStore
//đọc PDF, video, audio
//truy cập dữ liệu ứng dụng khác chia sẻ
//Nó giống như "cây cầu" → lấy dữ liệu từ một URI.

//openInputStream(uri) :MỞ URI + ĐỌC/LẤY DỮ LIỆU BÊN TRONG URI
//openInputStream() trả về null, phần bên trong sẽ không chạy.
// truyền this do tính năng của extension function, this là CLASS  mà nó mở rộng, ở đây this là Uri

//use : tự động đóng luồng sau khi hoàn thành công việc với luồng đó

//FileOutputStream(file): MỞ FILE THẬT + CHUẨN BỊ CHO GHI DỮ LIỆU VÀO

//copyTo : đọc từng chunk byte từ input,ghi sang output,lặp cho đến khi hết dữ liệu

//openInputStream đọc dữ liệu từ URI, FileOutputStream mở file để ghi, copyTo đổ toàn bộ dữ liệu từ URI vào file.

