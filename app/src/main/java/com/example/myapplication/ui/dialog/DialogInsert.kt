package com.example.myapplication.ui.dialog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.example.myapplication.base.BaseBottomSheet
import com.example.myapplication.databinding.DialogInsertBinding
import com.example.myapplication.ui.home.HomeViewModel


class DialogInsert( private val viewModel: HomeViewModel ) : BaseBottomSheet<DialogInsertBinding>(
    inflater = DialogInsertBinding::inflate
){
    private var selectedImageUri: Uri? = null


    override fun initView() {
        super.initView()
        // Sự kiện chọn ảnh từ thư viện
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Sự kiện POST dữ liệu lên server
        binding.btnUpload.setOnClickListener {
            val t = binding.edtTitle.text.toString()
            val d = binding.edtDescription.text.toString()
            selectedImageUri?.let { uri ->
                viewModel.uploadAndSync(uri, t, d)
            }

        }
    }
    // Launcher để lấy kết quả chọn ảnh
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.imgPreview.setImageURI(selectedImageUri) // Hiển thị ảnh vừa chọn để xem trước
        }
    }
}