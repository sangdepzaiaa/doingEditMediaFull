package com.example.myapplication.ui.video_to_audio.result

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.ItemAudioBinding
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.tap

class ResultAdapter :
    androidx.recyclerview.widget.ListAdapter<MediaFile, ResultAdapter.AudioViewHolder>(DiffCallback()) {

    // Callbacks có thể set sau
    private var onItemClick: ((MediaFile) -> Unit)? = null

    fun setOnItemClickListener(listener: (MediaFile) -> Unit) {
        onItemClick = listener
    }

    inner class AudioViewHolder(private val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaFile) {
            binding.tvFileName.text = item.name
            binding.tvFilesize.text = item.size.formatFileSize()
            binding.tvFileduration.text = item.duration.formatDuration()

            binding.root.tap { onItemClick?.invoke(item) }
            binding.btnMore.isVisible = false
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateData(newItems: List<MediaFile>) {
        submitList(newItems.toList()) // ListAdapter yêu cầu List, không phải MutableList
    }

    fun updateItem(updatedMediaFile: MediaFile) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == updatedMediaFile.id }
        if (index != -1) {
            currentList[index] = updatedMediaFile
            submitList(currentList)
        }
    }

    fun deleteItem(deletedMediaFile: MediaFile) {
        val currentList = currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == deletedMediaFile.id }
        if (index != -1) {
            currentList.removeAt(index)
            submitList(currentList)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile): Boolean {
            return oldItem.id == newItem.id && oldItem.name == newItem.name && oldItem.size == newItem.size && oldItem.duration == newItem.duration
        }
    }
}
