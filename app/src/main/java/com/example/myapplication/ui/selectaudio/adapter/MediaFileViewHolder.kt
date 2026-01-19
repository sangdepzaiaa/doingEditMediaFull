package com.example.myapplication.ui.selectaudio.adapter

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.base.BaseViewHolder
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.SelectionMode
import com.example.myapplication.databinding.ItemMediaFileBinding
import com.example.myapplication.utils.tap
import java.util.Locale
import java.util.concurrent.TimeUnit

class MediaFileViewHolder(
    override val binding: ItemMediaFileBinding,
    private val onClickItem: (Int) -> Unit
) : BaseViewHolder<MediaFile, ItemMediaFileBinding>(binding) {

    init {
        itemView.tap {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onClickItem(bindingAdapterPosition)
            }
        }
    }

    override fun onBindData(data: MediaFile, selectionMode: SelectionMode) {
        super.onBindData(data)
        binding.tvFileName.text = data.name
        val iconRes = R.drawable.music
        binding.ivIcon.setImageResource(iconRes)
        val durationStr = formatDuration(data.duration)
        val sizeStr = formatSize(data.size)
        binding.tvTimeFile.text = durationStr
        binding.tvCapacityFile.text = sizeStr

        // Select mode
        if (selectionMode == SelectionMode.MULTIPLE) {
            binding.tvSelectionIndicator.visibility = View.VISIBLE

            if (data.selectionOrder > 0) {
                binding.tvSelectionIndicator.text = data.selectionOrder.toString()
                binding.tvSelectionIndicator.background = ContextCompat.getDrawable(
                    itemView.context, R.drawable.bg_selection_indicator_selected
                )
            } else {
                binding.tvSelectionIndicator.text = ""
                binding.tvSelectionIndicator.background = ContextCompat.getDrawable(
                    itemView.context, R.drawable.bg_selection_indicator_unselected
                )
            }
        } else {
            binding.tvSelectionIndicator.visibility = View.GONE
        }
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
        }
    }
}