package com.example.myapplication.ui.no_internet

import android.content.Intent
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.utils.tap
import android.provider.Settings
import com.example.myapplication.databinding.ActivityNoInternetBinding
import com.example.myapplication.ui.splash.SplashActivity
import com.example.myapplication.utils.CheckInternet

class NoInternetActivity: BaseActivity<ActivityNoInternetBinding>(
    inflater = ActivityNoInternetBinding::inflate
){
    override fun onResume() {
        super.onResume()

    }

    override fun initView() {
        super.initView()
        if(CheckInternet.isNetworkConnected(this)){
            startActivity(Intent(this, SplashActivity::class.java))
            finishAffinity()
        }

        binding.tvRetry.tap {
            if (!CheckInternet.isNetworkConnected(this)){
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }
    }
}






//startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
//Lệnh này sẽ mở màn hình cài đặt Wi-Fi của thiết bị Android.