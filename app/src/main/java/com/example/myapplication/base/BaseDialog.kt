package com.example.myapplication.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import org.koin.core.qualifier._q

abstract class BaseDialog<VB: ViewBinding>(
    context: Context,
    var inflater: (LayoutInflater) -> VB
): Dialog(context,R.style.full_screen_dialog){
    protected val binding by lazy { inflater(layoutInflater) }

    init {
        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setOnKeyListener { _,onKey,onEvent ->
            if (onKey == KeyEvent.KEYCODE_BACK && onEvent.action == KeyEvent.ACTION_UP) true
            else false
        }
        initView()
        bindView()
    }

    override fun onStart() {
        super.onStart()

        val width = context.resources.displayMetrics.widthPixels
        val window = window ?: return
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        window.setDimAmount(0.5f)

    }

    open fun initView(){}
    open fun bindView(){}
}



