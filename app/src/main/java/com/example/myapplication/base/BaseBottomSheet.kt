package com.example.myapplication.base

import android.graphics.drawable.Drawable
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
    var inflater: (LayoutInflater, ViewGroup?, Boolean) -> VB
): BottomSheetDialogFragment(){
    private var _binding: VB? = null
    protected val binding:VB get() =  _binding!!

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
        dialog?.setOnKeyListener { _,onKey,onEvent ->
            if (onKey == KeyEvent.KEYCODE_BACK && onEvent.action == KeyEvent.ACTION_UP) true
            else false
        }

        ViewCompat.setOnApplyWindowInsetsListener(dialog?.window?.decorView){v,inset ->
            val imeHight = inset.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val imeVisibility = inset.isVisible(WindowInsetsCompat.Type.ime())
            val width = (resources.displayMetrics.density * 225).toInt()

            view.updateLayoutParams<ViewGroup.MarginLayoutParams>{
                bottomMargin = if(imeVisibility) imeHight
                else 0
            }
            inset
        }


        initView()
        bindView()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog as BottomSheetDialog
        val behavior = dialog.behavior
        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet)

        behavior.apply {
            skipCollapsed = true
            isHideable = true
            state = BottomSheetBehavior.STATE_EXPANDED
        }

        bottomSheet?.apply {
            setBackgroundResource(R.drawable.bg_round_8_border)
            clipToOutline = true
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
