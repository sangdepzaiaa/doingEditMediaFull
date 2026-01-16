package com.example.myapplication.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.widget.addTextChangedListener
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.DialogConfirmExitBinding
import com.example.myapplication.databinding.DialogDeleteResultBinding
import com.example.myapplication.databinding.DialogMenuMoreHistoryBinding
import com.example.myapplication.databinding.DialogMenuMoreResultBinding
import com.example.myapplication.databinding.DialogRenameResultBinding
import com.example.myapplication.databinding.DialogSavingBinding
import com.example.myapplication.databinding.DialogUploadImageErrorBinding
import com.example.myapplication.ui.result.ResultActivity
import com.example.myapplication.utils.share
import com.example.myapplication.utils.tap


object DialogHelper{

    var mediaFile: MediaFile? = null

    class SavingDialog(context: Context) : BaseDialog<DialogSavingBinding>(context,
        inflater = DialogSavingBinding::inflate
    ) {
        override fun initView() {
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            val rotate = AnimationUtils.loadAnimation(context, R.anim.rotate)
            binding.progressBar.startAnimation(rotate)
            window?.setGravity(Gravity.CENTER)
        }


    }

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

    fun showPopupResult(
        anchor: View,
        context: Context,
        onCutter: () -> Unit,
        onRename: () -> Unit,
        deleteMediaFile: () -> Unit
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val popupBinding = DialogMenuMoreResultBinding.inflate(layoutInflater)

        val popupWindow = PopupWindow(
            popupBinding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            isOutsideTouchable = true
        }

        popupBinding.apply {
            tvCutter.tap {
                onCutter()
                popupWindow.dismiss()
            }
            tvRename.tap {
                onRename()
                popupWindow.dismiss()
            }
            tvDelete.tap {
                deleteMediaFile()
                popupWindow.dismiss()
            }
        }

        showPopUpAtLocation(anchor, popupBinding, popupWindow)
    }

    private fun showPopUpAtLocation(
        anchor: View, popupBinding: ViewBinding, popupWindow: PopupWindow
    ) {
        // Tính vị trí của anchor view trên màn hình
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]

        // Đo kích thước popup
        popupBinding.root.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        val popupWidth = popupBinding.root.measuredWidth
        val popupHeight = popupBinding.root.measuredHeight

        // Tính vị trí hiển thị popup (đè lên anchor, căn phải)
        val xOffset = anchorX + anchor.width - popupWidth
//        val yOffset = anchorY - anchor.height / 2
        val yOffset = when (popupBinding) {
            is DialogMenuMoreHistoryBinding -> anchorY - anchor.height / 2
            is DialogMenuMoreResultBinding -> anchorY - anchor.height / 3
            else -> anchorY + anchor.height
        }
        // cách anchor 10dp bên trái
        //        val xOffset = anchorX - popupWidth
        //        val yOffset = anchorY

        // Hiển thị popup tại vị trí tính toán
        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xOffset, yOffset)
    }

    fun showRenameDialog(
        context: Context,
        currentName: String,
        onSaveComplete: (() -> Unit)?,
        onRename: (String) -> Unit = {}
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogRenameResultBinding.inflate(layoutInflater)
        val dialog =
            AlertDialog.Builder(context).setView(binding.root).setCancelable(false).create()
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        // Ban đầu Save luôn cam (enable)
        binding.btnSave.isEnabled = true
        binding.btnSave.background =
            ContextCompat.getDrawable(context, R.drawable.bg_button_rename_result_enable)

        binding.edtName.setText(currentName)

//        // Khi EditText được focus lần đầu → Save chuyển xám
//        binding.edtName.setOnFocusChangeListener { _, hasFocus ->
//            if (hasFocus) {
//                binding.btnSave.isEnabled = false
//                binding.btnSave.background =
//                    ContextCompat.getDrawable(context, R.drawable.bg_button_rename_result_disable)
//            }
//        }

        // Khi người dùng nhập text → Save cam
        binding.edtName.addTextChangedListener {
            val hasText = !it.isNullOrBlank()
            binding.btnSave.isEnabled = hasText
            binding.btnSave.background = ContextCompat.getDrawable(
                context, if (hasText) {
                    R.drawable.bg_button_rename_result_enable
                } else {
                    R.drawable.bg_round_8_1b1b1b
                }
            )
        }

        binding.btnCancel.tap { dialog.dismiss() }
        binding.btnSave.tap {
            val newName = binding.edtName.text.toString().trim()
            if (newName.isNotEmpty()) {
                onRename(newName)
                onSaveComplete?.invoke()
                dialog.dismiss()
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun showDeleteDialog(context: Context, onItemDelete: () -> Unit) {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogDeleteResultBinding.inflate(layoutInflater)
        val dialog = Dialog(context)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        binding.btnCancel.tap { dialog.dismiss() }
        binding.btnSave.tap {
            onItemDelete()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun showExitDialogDialog(
        context: Context, title: String? = null, content: String? = null,
        cancelText: String? = null, confirmText: String? = null, onConfirm: () -> Unit
    ) {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogConfirmExitBinding.inflate(layoutInflater)
        val dialog = Dialog(context)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        if (title != null) {
            binding.tvTitle.text = title
        }

        if (content == null) binding.edtName.isSingleLine = true
        else {
            binding.edtName.apply {
                text = content
                if (content.length > 20 || content.contains("\n")) {
                    isSingleLine = false
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                }
            }
        }

        if (cancelText != null) binding.btnCancel.text = cancelText
        if (confirmText.isNullOrEmpty().not()) binding.btnSave.text = confirmText

        binding.btnCancel.tap { dialog.dismiss() }
        binding.btnSave.tap {
            onConfirm()
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    fun showPopupHistory(
        anchor: View,
        context: Context,
        curentMediaFile: MediaFile,
        onRename: (String) -> Unit,
        onItemDelete: (MediaFile) -> Unit,
    ) {
        mediaFile = curentMediaFile
        val layoutInflater = LayoutInflater.from(context)
        val popupBinding = DialogMenuMoreHistoryBinding.inflate(layoutInflater)

        // Tính chiều rộng popup (90% màn hình)
        val popupWidth = (anchor.context.resources.displayMetrics.widthPixels * 0.9).toInt()

        // Đo kích thước của popup
        popupBinding.root.measure(
            View.MeasureSpec.makeMeasureSpec(popupWidth, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
//        val popupHeight = popupBinding.root.measuredHeight

        val popupWindow = PopupWindow(
            popupBinding.root,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popupWindow.isOutsideTouchable = true



        popupBinding.tvRename.tap {
            showRenameDialog(
                context, currentName = mediaFile?.name!!, null, onRename = { onRename(it) })
            popupWindow.dismiss()
        }
        popupBinding.tvPlayer.tap {
            ResultActivity.newIntent(context, mediaFile!!).also { context.startActivity(it) }
            popupWindow.dismiss()
        }
        popupBinding.tvDelete.tap {
          showDeleteDialog(
                context
            ) {
                onItemDelete(curentMediaFile)
            }
            popupWindow.dismiss()
        }
        popupBinding.tvShare.tap {
            mediaFile?.share(context)
            popupWindow.dismiss()
        }
        showPopUpAtLocation(
            anchor,
            popupBinding,
            popupWindow
        )
    }

}
