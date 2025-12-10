package com.example.myapplication.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xxx.faceswap.doingeditmediafull.R
import kotlin.random.Random

abstract class BaseBottomSheet<VB: ViewBinding>(
    val inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB,
): BottomSheetDialogFragment(){

    private var _binding:VB ?= null
    protected val binding :VB get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflater(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCancelable(true)
        dialog?.setCanceledOnTouchOutside(true)
        initView()
        bindView()
    }

    override fun getTheme(): Int = R.style.AppBottomSheetDialogTheme

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        dialog?.dismiss()
        super.onDetach()
    }
    open fun initView(){}
    open fun bindView(){}
}