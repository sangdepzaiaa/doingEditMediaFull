package com.example.myapplication.ui.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.net.Uri
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
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.myapplication.R
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.PermissionType
import com.example.myapplication.data.local.dao.history.MediaFileRepository
import com.example.myapplication.databinding.DialogAudioOptionsBinding
import com.example.myapplication.databinding.DialogConfirmExitBinding
import com.example.myapplication.databinding.DialogDeleteResultBinding
import com.example.myapplication.databinding.DialogInfoBinding
import com.example.myapplication.databinding.DialogMenuMoreHistoryBinding
import com.example.myapplication.databinding.DialogMenuMoreResultBinding
import com.example.myapplication.databinding.DialogRenameResultBinding
import com.example.myapplication.databinding.DialogSavingBinding
import com.example.myapplication.databinding.DialogSetAsItemHomeBinding
import com.example.myapplication.databinding.DialogUploadImageErrorBinding
import com.example.myapplication.ui.result.ResultActivity
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.RingtoneUtils
import com.example.myapplication.utils.deleteFile
import com.example.myapplication.utils.formatDate
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.getFolderName
import com.example.myapplication.utils.getPathName
import com.example.myapplication.utils.share
import com.example.myapplication.utils.tap
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get


object DialogHelper{

    var mediaFile: MediaFile? = null

    fun showPopup(
        context: Context,
        mediaFile: MediaFile,
        onRename: (String) -> Unit,
        onSaveComplete: () -> Unit,
        onContactPicker: ((MediaFile) -> Unit)? = null
    ) {
        DialogHelper.mediaFile = mediaFile
        val layoutInflater = LayoutInflater.from(context)
        val popupBinding = DialogAudioOptionsBinding.inflate(layoutInflater)

        popupBinding.tvFileName.text = mediaFile.name

        val popupWindow =
            AlertDialog.Builder(context).setView(popupBinding.root).setCancelable(false).create()
        popupWindow.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        // Click listeners
//        popupBinding.btnCutter.tap {
//            if (mediaFile == null) return@tap
//            CutAudioActivity.newIntent(
//                context, mediaFile!!, editType = EditType.AUDIO_CUTTER, onSaveComplete
//            ).also {
//                context.startActivity(it)
//            }
//            popupWindow.dismiss()
//        }
        popupBinding.btnSetRingtone.tap {
            showSetAsDialog(context, mediaFile, onContactPicker)
            popupWindow.dismiss()
        }
//        popupBinding.btnVolume.tap {
//            showBoostVolumeDialog(
//                context = context, mediaFile = mediaFile, onAudioBoostComplete = { boostedFile ->
//                    ResultActivity.newIntent(context = context, boostedFile).also {
//                        context.startActivity(it)
//                    }
//                }, onSaveComplete = onSaveComplete
//            )
//            popupWindow.dismiss()
//        }
        popupBinding.btnRename.tap {
            showRenameDialog(
                context,
                currentName = mediaFile.name,
                onSaveComplete = onSaveComplete,
                onRename = { onRename(it) }
            )
            popupWindow.dismiss()
        }
        popupBinding.btnPlayer.tap {
            ResultActivity.newIntent(context = context, mediaFile!!).also {
                context.startActivity(it)
            }
            popupWindow.dismiss()
        }
        popupBinding.btnDelete.tap {
            showDeleteDialog(
                context, mediaFile, onSaveComplete = onSaveComplete
            )
            popupWindow.dismiss()
        }
        popupBinding.btnShare.tap {
            mediaFile.share(context)
            popupWindow.dismiss()
        }
        popupBinding.btnInfo.tap {
            setInfo(context, mediaFile)
            popupWindow.dismiss()
        }
        popupBinding.ivClose.tap {
            popupWindow.dismiss()
        }

////        // Hiện popup ngay dưới anchor
////        popupWindow.showAsDropDown(anchor, 0, 0)
//
//        // Tính toán tọa độ để căn giữa
//        val screenWidth = anchor.context.resources.displayMetrics.widthPixels
//        val screenHeight = anchor.context.resources.displayMetrics.heightPixels
//        val x = (screenWidth - popupWidth) / 2
//        val y = (screenHeight - popupHeight) / 2
//
//        // Hiển thị popup ở giữa màn hình
//
//        popupWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, x, y)
        popupWindow.show()
        popupWindow.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showSetAsDialog(context: Context, mediaFile: MediaFile, onContactPicker: ((MediaFile) -> Unit)? = null) {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogSetAsItemHomeBinding.inflate(layoutInflater)

        val dialog = Dialog(context)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

        binding.btnCancel.tap { dialog.dismiss() }

        binding.btnSave.tap {
            dialog.dismiss()

            // Get the selected radio button from the RadioGroup
            when (binding.radioGroup.checkedRadioButtonId) {
                binding.rabRingtone.id -> PermissionUtils.checkAndRequestAllPermissions(
                    context as Activity, PermissionType.RINGTONES.value
                ) {
                    RingtoneUtils.setAsRingtone(context, mediaFile)
                }

                binding.rabAlarmTone.id -> PermissionUtils.checkAndRequestAllPermissions(
                    context as Activity, PermissionType.ALARMS.value
                ) {
                    RingtoneUtils.setAsAlarm(context, mediaFile)
                }

                binding.rabNotification.id -> PermissionUtils.checkAndRequestAllPermissions(
                    context as Activity, PermissionType.NOTIFICATIONS.value
                ) {
                    RingtoneUtils.setAsNotification(context, mediaFile)
                }

                binding.rabContact.id -> PermissionUtils.checkAndRequestAllPermissions(
                    context as Activity, PermissionType.CONTACTS.value
                ) {
                    if (onContactPicker != null) {
                        // Sử dụng callback từ Activity nếu có
                        onContactPicker(mediaFile)
                    } else {
                        // Fallback: chỉ mở danh bạ
                        try {
                            val intent = RingtoneUtils.getContactPickerIntent()
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.cannot_open_contacts),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        dialog.show()
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.9).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }


    private fun setInfo(context: Context, mediaFile: MediaFile) {
        val layoutInflater = LayoutInflater.from(context)
        val dialogBinding = DialogInfoBinding.inflate(layoutInflater)

        val dialog =
            AlertDialog.Builder(context).setView(dialogBinding.root).setCancelable(false).create()

        dialogBinding.apply {
            tvvFolderValue.text = mediaFile.uri.getFolderName(context)
            tvvSizeValue.text = mediaFile.size.formatFileSize()
            tvvModifiedValue.text = mediaFile.dateAdded.formatDate()
            tvvPathValue.text = mediaFile.uri.getPathName(context)
        }
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.8).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        dialogBinding.ivClose.tap {
            dialog.dismiss()
        }
    }



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

    private fun showDeleteDialog(
        context: Context, mediaFile: MediaFile, onSaveComplete: (() -> Unit)?
    ) {
        val mediaFileRepository =
            (context.applicationContext as KoinComponent).get<MediaFileRepository>()
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogDeleteResultBinding.inflate(layoutInflater)
        val dialog = Dialog(context)
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        binding.btnCancel.tap { dialog.dismiss() }
        binding.btnSave.tap {
            val scope = (context as? LifecycleOwner)?.lifecycleScope
            scope?.launch {
                try {
                    mediaFileRepository.deleteMediaById(mediaFile.id)
                    context.deleteFile(mediaFile.uri)
                    onSaveComplete?.invoke()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
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

}
