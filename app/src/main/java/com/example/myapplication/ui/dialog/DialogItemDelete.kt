package com.example.myapplication.ui.dialog

import  android.content.Context
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.data.model.ImageEntity
import com.example.myapplication.databinding.DialogItemDeleteBinding
import com.example.myapplication.ui.home.HomeViewModel

import com.example.myapplication.utils.tap

class DialogItemDelete(
    context: Context,
    private val viewModel: HomeViewModel,
    private val item: ImageEntity,
) : BaseDialog<DialogItemDeleteBinding>(context, inflater = DialogItemDeleteBinding::inflate) {

    override fun initView() {
        binding.tvNo.tap {
            dismiss()
        }
        binding.tvYes.setOnClickListener {
            viewModel.deleteImage(item)
            dismiss()
        }
        binding.tvClose.tap {
            dismiss()
        }
    }
}