package com.example.myapplication.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.ItemAudioBinding
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.gone
import com.example.myapplication.utils.tap
import com.example.myapplication.utils.visible

class MediaFileAdapter(private val isSavingProgress: Boolean = false) :
    ListAdapter<MediaFile, MediaFileAdapter.MediaViewHolder>(DiffCallback()) {

    // Callbacks có thể set sau khi tạo adapter
    private var onItemClick: ((MediaFile) -> Unit)? = null
    private var onMoreClick: ((Pair<MediaFile, View>) -> Unit)? = null

    fun setOnMoreListener(listener: (Pair<MediaFile, View>) -> Unit) {
        onMoreClick = listener
    }

    fun setonItemClickListener(listener: (MediaFile) -> Unit) {
        onItemClick = listener
    }

    inner class MediaViewHolder(private val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaFile) {
            if (isSavingProgress)
                binding.btnMore.gone()
            else
                binding.btnMore.visible()
            binding.tvFileName.text = item.name
            binding.tvFileduration.text = item.duration.formatDuration()
            binding.tvFilesize.text = item.size.formatFileSize()

            binding.tvFileName.tap {
                onItemClick?.invoke(item)
            }
            binding.btnMore.tap {
                onMoreClick?.invoke(item to binding.btnMore)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) = oldItem == newItem
    }
}
