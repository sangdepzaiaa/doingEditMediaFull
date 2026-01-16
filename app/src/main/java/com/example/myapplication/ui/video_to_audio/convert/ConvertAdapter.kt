package com.example.myapplication.ui.video_to_audio.convert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemConvertFileBinding
import com.example.myapplication.ui.video_to_audio.video_to_audio.VideoItem
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.loadImage
import com.example.myapplication.utils.tap

class ConvertAdapter : ListAdapter<VideoItem, ConvertAdapter.ConvertViewHolder>(DiffCallback()) {

    var onDeleteClick: ((VideoItem) -> Unit)? = null

    inner class ConvertViewHolder(private val b: ItemConvertFileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: VideoItem) {
            b.ivThumbnail.loadImage(item.uri, R.drawable.banner_audio)

            b.tvFileName.text = item.name
            b.tvFileDuration.text = item.duration.formatDuration()
            b.tvFileSize.text = item.size.formatFileSize()
            b.tvTyleFile.text = item.getFileType()
            b.btnDelete.tap { onDeleteClick?.invoke(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ConvertViewHolder(ItemConvertFileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ConvertViewHolder, position: Int) =
        holder.bind(getItem(position))

    class DiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem) =
            oldItem.uri.toString() == newItem.uri.toString()

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem) = oldItem == newItem
    }
}

