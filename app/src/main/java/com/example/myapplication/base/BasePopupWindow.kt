package com.example.myapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding

abstract class BasePopupWindow<VB : ViewBinding>(
    context: Context,
    val inflater: (LayoutInflater) -> VB,
    var heightScreen: Int = WindowManager.LayoutParams.WRAP_CONTENT,
    var widthScreen:Int = WindowManager.LayoutParams.WRAP_CONTENT
): PopupWindow(context){

    protected val binding:VB by lazy { inflater(LayoutInflater.from(context)) }

    init {
        contentView = binding.root
        isFocusable = true
        isOutsideTouchable = true
        this.height = heightScreen
        this.width = widthScreen
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        initView()
        bindView()
    }
    open fun initView(){}
    open fun bindView(){}
}