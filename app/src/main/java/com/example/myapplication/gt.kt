package com.example.myapplication

import android.view.View
import androidx.core.view.WindowInsetsCompat

//@Suppress("DEPRECATION")
//protected fun hideNavigationrBarLowerAndroid(){
//    window.decorView.systemUiVisibility =
//        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//
//                View.SYSTEM_UI_FLAG_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//
//}
//@SuppressLint("InlinedApi") tắt cảnh báo không an toàn với phiên bản SDK hiện tại.
//@Suppress("DEPRECATION") : tắt cảnh baó deprecation tech
//window.decorView.systemUiVisibility : hệ thống hiển thị UI
//View.SYSTEM_UI_FLAG_HIDE_NAVIGATION : ẩn navigation
//View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION : cho lay vẽ dưới navigation
//View.SYSTEM_UI_FLAG_FULLSCREEN: ẩn status bar
//View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: cho lay vẽ dưới status bar
//View.SYSTEM_UI_FLAG_LAYOUT_STABLE: layout không bị giật khi ẩn/hiện system bars
//View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY: vuốt hiện system bars tạm thời, rồi ẩn lại sau vài giây

//WindowCompat.setDecorFitsSystemWindows(window, false)
//WindowInsetsControllerCompat(window, binding.root).apply {
//    // Ẩn cả navigation bar và status bar
//    hide(WindowInsetsCompat.Type.systemBars())
//    // Immersive sticky: vuốt tạm hiện bar, tự ẩn lại
//    systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
//}
//Type.systemBars() = navigationBars + statusBars → tương đương HIDE_NAVIGATION + FULLSCREEN
//setDecorFitsSystemWindows(false) → tương đương LAYOUT_HIDE_NAVIGATION + LAYOUT_FULLSCREEN + LAYOUT_STABLE
//systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE → tương đương IMMERSIVE_STICKY

//nâng layoput ở đáy khi ime và nav xuất hiện
//ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
//    val systemBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
//    val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
//    val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())
//    v.setPadding(
//        systemBars.left,
//        systemBars.top,
//        systemBars.right,
//        navBars.bottom + imeInsets.bottom
//    )
//    insets
//}


