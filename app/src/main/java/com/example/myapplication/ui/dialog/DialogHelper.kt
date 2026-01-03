package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.databinding.DialogUploadImageErrorBinding
import com.example.myapplication.utils.tap


class UploadErrorDialog(
    context: Context,
    private val action: () -> Unit,
    private val dismissAction: (() -> Unit)? = null
) : BaseDialog<DialogUploadImageErrorBinding>(context, DialogUploadImageErrorBinding::inflate) {



    override fun bindView() {
        binding.tvTryAgain.tap {
            action()
            dismiss()
        }
        binding.icClose.tap {
            dismissAction?.invoke()
            dismiss()
        }
    }
}