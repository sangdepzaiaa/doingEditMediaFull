package com.example.myapplication.ui.dialog

import android.content.DialogInterface
import android.view.View
import com.example.myapplication.base.BaseBottomSheet
import com.example.myapplication.databinding.DialogCheckFaceIdBinding
import com.example.myapplication.utils.tap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog


class DialogCheckFaceId(
    private val content: String? = null,
    private val title: String? = null,
    private val action: () -> Unit
) : BaseBottomSheet<DialogCheckFaceIdBinding>(
    { inflater, parent, attach ->
        DialogCheckFaceIdBinding.inflate(inflater, parent, attach)
    }
) {

    private var onDismissListener: (() -> Unit)? = null
    private var onAllowListener: (() -> Unit)? = null

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    fun setOnAllowListener(listener: () -> Unit) {
        onAllowListener = listener
    }

    override fun initView() {
        // Disable dragging
        (dialog as? BottomSheetDialog)?.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
            ?.let { sheet ->
                BottomSheetBehavior.from(sheet).isDraggable = false
            }

        // Set Text
        title?.let { binding.tvAllowNotification.text = it }
        content?.let { binding.tvWillBeNotification.text = it }
    }

    override fun bindView() {
        binding.tvDone.tap {
            action()
            onAllowListener?.invoke()
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }
}