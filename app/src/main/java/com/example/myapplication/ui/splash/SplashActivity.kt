package com.example.myapplication.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.ui.home.HomeActivity
import com.example.myapplication.ui.permission.PermissionActivity
import com.example.myapplication.utils.SharePreUtils
import com.example.myapplication.utils.const
import com.xxx.faceswap.doingeditmediafull.R
import com.xxx.faceswap.doingeditmediafull.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity<ActivitySplashBinding>(inflater = ActivitySplashBinding::inflate){

    val list = listOf(
        R.string.text_splash_1,
        R.string.text_splash_2,
        R.string.text_splash_3,
        R.string.text_splash_4
    )

    fun setSplashText(){
        lifecycleScope.launch {
            for(i in list){
                binding.tvSplash.apply {
                    alpha = 0f
                    text = getString(i)
                    animate().alpha(1f).setDuration(250).start()
                }
                delay(500)
            }
        }
    }

    override fun initView() {
        super.initView()
        setSplashText()

        lifecycleScope.launch {
            delay(3000)

            val hasPrefs = SharePreUtils.getBoolean(this@SplashActivity, const.CHECK_PERMISSION,false)
            if (!hasPrefs){
                SharePreUtils.setBoolean(this@SplashActivity, const.CHECK_PERMISSION,true)
                startActivity(Intent(this@SplashActivity, PermissionActivity::class.java))
            }else{
                startActivity(Intent(this@SplashActivity, HomeActivity::class.java))
                finish()
            }
        }
    }
}

//     lifecycleScope.launch { tự hủy khi activity bị destroy, không leak memory
