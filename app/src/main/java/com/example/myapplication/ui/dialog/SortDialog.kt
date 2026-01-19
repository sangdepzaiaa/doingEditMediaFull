package com.example.myapplication.ui.dialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.myapplication.R
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.data.enumm.SortCriteria
import com.example.myapplication.data.enumm.SortOrder
import com.example.myapplication.databinding.DialogSortBinding
import com.example.myapplication.ui.selectaudio.view_model.MediaViewModel
import com.example.myapplication.utils.dpToPx
import com.example.myapplication.utils.tap

class SortDialog(
    context: Context,
    private val viewModel: MediaViewModel
) : BaseDialog<DialogSortBinding>(context, inflater = DialogSortBinding::inflate) {



    override fun initView() {
        super.initView()

        window?.let { window ->
            window.setGravity(Gravity.BOTTOM)
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            val displayMetrics = context.resources.displayMetrics
            val width = displayMetrics.widthPixels - (44.dpToPx())

            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Set margin bottom
            val layoutParams = window.attributes
            layoutParams.y = 22.dpToPx()
            window.attributes = layoutParams
        }

        when (viewModel.sortCriteria.value) {
            SortCriteria.DURATION -> binding.rgSortBy.check(R.id.rbDuration)
            SortCriteria.NAME -> binding.rgSortBy.check(R.id.rbName)
            SortCriteria.DATE -> binding.rgSortBy.check(R.id.rbDate)
            else -> {} // Handle null
        }

        when (viewModel.sortOrder.value) {
            SortOrder.ASCENDING -> binding.rgSortOrder.check(R.id.rbAscending)
            SortOrder.DESCENDING -> binding.rgSortOrder.check(R.id.rbDescending)
            else -> {} // Handle null
        }
    }

    override fun bindView() {
        super.bindView()
        binding.btnCancel.tap {
            dismiss()
        }

        binding.btnSave.tap {
            val selectedCriteria = when (binding.rgSortBy.checkedRadioButtonId) {
                R.id.rbName -> SortCriteria.NAME
                R.id.rbDate -> SortCriteria.DATE
                else -> SortCriteria.DURATION
            }

            val selectedOrder = when (binding.rgSortOrder.checkedRadioButtonId) {
                R.id.rbAscending -> SortOrder.ASCENDING
                else -> SortOrder.DESCENDING
            }

            viewModel.applySort(selectedCriteria, selectedOrder)
            dismiss()
        }
    }
}