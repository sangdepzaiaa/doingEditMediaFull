package com.example.myapplication.ui.dialog

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment.STYLE_NORMAL
import com.example.myapplication.R
import com.example.myapplication.base.BaseBottomSheet
import com.example.myapplication.databinding.DialogFeedBackBinding
import com.example.myapplication.utils.tap
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogFeedback : BaseBottomSheet<DialogFeedBackBinding>(
    inflater = DialogFeedBackBinding::inflate
) {

    override fun initView() {
        super.initView()

        binding.icDismiss.tap {
            dialog?.dismiss()
        }
        binding.btnSave.tap {
            val subject = binding.etSubject.text.toString()
            val yourFeedback = binding.etEnterYourFeedback.text.toString()
            if (subject.isNotEmpty() || yourFeedback.isNotEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("v1studio.0225@gmail.com"))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, yourFeedback)
                    setPackage("com.google.android.gm")
                }
                try {
                    startActivity(emailIntent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Gmail app is not installed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}