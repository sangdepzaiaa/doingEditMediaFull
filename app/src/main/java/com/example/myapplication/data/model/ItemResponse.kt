package com.example.myapplication.data.model

data class ItemResponse(
    val id: String,
    val title: String,
    val description: String,
    val image_url: String
)




// thêm plugins {
//    id("kotlin-parcelize")
//}

//Parcelable là một interface trong Android dùng để đóng gói (serialize) dữ liệu thành dạng nhị phân
// để có thể truyền qua các IPC (Inter-Process Communication) hoặc giữa các component (Activity, Fragment, Service)
