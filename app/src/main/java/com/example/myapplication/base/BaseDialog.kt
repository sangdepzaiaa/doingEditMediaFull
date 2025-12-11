package com.example.myapplication.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R



abstract class BaseDialog<VB : ViewBinding>(
    context: Context,
    val inflater : (layoutInflater : LayoutInflater) -> VB,
): Dialog(context, R.style.full_screen_dialog){

    protected val binding by lazy { inflater(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        initView()
        bindView()
    }

    override fun onStart() {
        super.onStart()
        val window = this.window ?: return
        val displayMetrics = context.resources.displayMetrics
        val width = (displayMetrics.widthPixels*0.5).toInt()
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.setDimAmount(0.5f)
    }
    open fun initView(){}
    open fun bindView(){}
}