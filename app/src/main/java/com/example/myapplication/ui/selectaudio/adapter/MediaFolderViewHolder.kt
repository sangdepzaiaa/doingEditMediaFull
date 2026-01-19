package com.example.myapplication.ui.selectaudio.adapter

import com.example.myapplication.base.BaseViewHolder
import com.example.myapplication.data.model.MediaFolder
import com.example.myapplication.databinding.ItemMediaFolderBinding
import com.example.myapplication.utils.tap

class MediaFolderViewHolder(
    override val binding: ItemMediaFolderBinding,
    private val onItemClicked: (MediaFolder) -> Unit // Lambda for handling clicks
) : BaseViewHolder<MediaFolder, ItemMediaFolderBinding>(binding) {
    override fun onBindData(data: MediaFolder) {
        super.onBindData(data)
        binding.tvFolderName.text = data.name
        itemView.tap {
            this.data?.let { folder ->
                onItemClicked(folder)
            }
        }
    }
}