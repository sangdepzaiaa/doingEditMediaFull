package com.example.myapplication.ui.main

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
import com.example.myapplication.utils.tap

class SavedAudioAdapter : ListAdapter<MediaFile, SavedAudioAdapter.AudioViewHolder>(DiffCallback()) {

    var onMoreClick: ((MediaFile, View) -> Unit)? = null

    var onItemClick: ((MediaFile) -> Unit)? = null

    fun setOnMoreClickListener(listener: (MediaFile, View) -> Unit) {
        onMoreClick = listener
    }

    fun setOnItemClickListener(listener: ( MediaFile) -> Unit) {
        onItemClick = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val binding = ItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AudioViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AudioViewHolder(private val binding: ItemAudioBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MediaFile) {
            binding.tvFileName.text = item.name
            binding.tvFileduration.text = item.duration.formatDuration()
            binding.tvFilesize.text = item.size.formatFileSize()
            binding.btnMore.tap {
                onMoreClick?.invoke(item, binding.btnMore)
            }
            binding.root.tap {
                onItemClick?.invoke(item)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MediaFile>() {
        override fun areItemsTheSame(oldItem: MediaFile, newItem: MediaFile) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MediaFile, newItem: MediaFile) = oldItem == newItem
    }
}