package com.example.myapplication.ui.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.viewbinding.ViewBinding

abstract class BasePopupWindow<VB: ViewBinding>(
    context: Context,
    inflater: (LayoutInflater) -> VB,
    wid :Int = WindowManager.LayoutParams.WRAP_CONTENT,
    hei : Int = WindowManager.LayoutParams.WRAP_CONTENT
): PopupWindow(context){
    protected val binding by lazy { inflater(LayoutInflater.from(context)) }
    init {
        contentView = binding.root
        width = wid
        height = hei
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        isTouchable = true
        initView()
        bindView()
    }
    open fun initView(){}
    open fun bindView(){}
}