package com.example.myapplication.ui.selectaudio.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.example.myapplication.base.BaseListAdapter
import com.example.myapplication.data.model.MediaFolder
import com.example.myapplication.databinding.ItemMediaFolderBinding

class MediaFolderAdapter(
    private val onItemClicked: (MediaFolder) -> Unit
) : BaseListAdapter<MediaFolder, ItemMediaFolderBinding, MediaFolderViewHolder>(FolderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFolderViewHolder {
        val binding = ItemMediaFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MediaFolderViewHolder(binding, onItemClicked)
    }
}

class FolderDiffCallback : DiffUtil.ItemCallback<MediaFolder>() {
    override fun areItemsTheSame(oldItem: MediaFolder, newItem: MediaFolder): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: MediaFolder, newItem: MediaFolder): Boolean {
        return oldItem == newItem
    }
}