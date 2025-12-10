package com.example.myapplication.ui.dialog

import android.view.View
import com.example.myapplication.base.BaseBottomSheet
import com.example.myapplication.utils.tap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xxx.faceswap.doingeditmediafull.databinding.DialogChoosePhotoBinding

class DialogTypeChoosePhoto(
    private val selectedListener: OnSelectedListener? = null
) : BaseBottomSheet<DialogChoosePhotoBinding>(
    DialogChoosePhotoBinding::inflate
) {

    override fun initView() {
        // Disable kéo xuống
        (dialog as? BottomSheetDialog)?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )?.let { sheet ->
            BottomSheetBehavior.from(sheet).apply {
                isDraggable = false
            }
        }
    }

    override fun bindView() {
        binding.vImage.tap { safeDismissAndSelect { selectedListener?.onPhotoSelected() } }
        binding.vCamera.tap { safeDismissAndSelect { selectedListener?.onCameraSelected() } }
        binding.icClose.tap { safeDismissAndSelect { } }   // chỉ đóng
    }

    // Hàm siêu quan trọng – gọi cái này thay cho dismiss() + callback
    private inline fun safeDismissAndSelect(crossinline action: () -> Unit) {
        // 1. Đảm bảo chỉ gọi action một lần duy nhất
        // 2. Dùng dismissAllowingStateLoss() để không bao giờ crash
        // 3. Kiểm tra isAdded tránh trường hợp cực hiếm
        if (isAdded) {
            action()                                           // thực hiện hành động trước
            dismissAllowingStateLoss()                         // rồi mới đóng, an toàn tuyệt đối
        } else {
            // Fragment đã detach → vẫn gọi callback để không mất hành động người dùng
            action()
        }
    }

    interface OnSelectedListener {
        fun onPhotoSelected()
        fun onCameraSelected()
    }
}