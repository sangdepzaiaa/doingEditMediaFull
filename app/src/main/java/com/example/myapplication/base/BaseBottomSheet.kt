package com.example.myapplication.base

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

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
        dialog?.apply {
            setCancelable(true)
            setCanceledOnTouchOutside(true)
            setOnKeyListener { _,onKey,onEvent ->
                if (onKey == KeyEvent.KEYCODE_BACK && onEvent.action == KeyEvent.ACTION_UP) true
                else false
            }
        }


        initView()
        bindView()

        ViewCompat.setOnApplyWindowInsetsListener(dialog?.window!!.decorView){_, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

            val display = (225 * resources.displayMetrics.density).toInt()

            view.updateLayoutParams<ViewGroup.MarginLayoutParams>{
              bottomMargin = if(imeVisible) imeHeight
                             else 0
            }
            insets
        }
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val behavior = dialog.behavior
        val bottomSheet = dialog.findViewById<View>( com.google.android.material.R.id.design_bottom_sheet)

        bottomSheet?.apply {
            setBackgroundResource(R.drawable.bg_round_8_border)
            clipToOutline = true
        }

        behavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isHideable = true
        }
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