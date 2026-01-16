package com.example.myapplication.ui.dialog

import android.content.Context
import android.net.Uri
import com.bumptech.glide.Glide
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.data.model.ImageEntity
import com.example.myapplication.databinding.DialogItemUpdateBinding
import com.example.myapplication.ui.home.HomeViewModel


class DialogItemUpdate(
    context: Context,
    private val viewModel: HomeViewModel,
    private val item: ImageEntity,
    private val onPickImageClick: () -> Unit
) : BaseDialog<DialogItemUpdateBinding>(context, inflater = DialogItemUpdateBinding::inflate) {
    private var selectedImageUri: Uri? = null

    override fun initView() {
        super.initView()

        binding.imgSelectImage.setOnClickListener {
            onPickImageClick.invoke()
        }

        binding.btnUpdateItem.setOnClickListener {
            // Lấy ID từ item cũ để đảm bảo đây là lệnh UPDATE
            val id = item.id
            val title = binding.edtFileName.text.toString()
            val desc = binding.edtSize.text.toString()

            viewModel.updateImage(item, selectedImageUri, title, desc)
        }
    }

    // 2. PHẢI gán uri vào biến selectedImageUri ở đây
    fun setImagePreview(uri: Uri) {
        this.selectedImageUri = uri // Lưu lại để tí nữa lấy ra toString()
        Glide.with(context).load(uri).into(binding.imgSelectImage)
    }
}