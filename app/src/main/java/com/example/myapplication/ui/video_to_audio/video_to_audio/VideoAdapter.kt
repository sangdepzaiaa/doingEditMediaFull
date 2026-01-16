package com.example.myapplication.ui.video_to_audio.video_to_audio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.example.myapplication.R
import com.example.myapplication.base.BaseListAdapter
import com.example.myapplication.base.BaseViewHolder
import com.example.myapplication.databinding.ItemVideoBinding
import com.example.myapplication.utils.const.MAX_SELECTION_COUNT
import com.example.myapplication.utils.const.MIN_SELECTED_MEDIA_DURATION_MS
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.loadImage
import com.example.myapplication.utils.tap


class VideoAdapter : BaseListAdapter<VideoItem, ItemVideoBinding, VideoAdapter.VideoViewHolder>(DiffCallback()) {

    private val selectedItems = mutableListOf<VideoItem>()
    private var onMultiSelectionChanged: (List<VideoItem>) -> Unit = {}
    private var onLimitReached: () -> Unit = {}
    private var onLessThanLimitReached: () -> Unit = {}

    fun setOnLimitReached(listener: () -> Unit) {
        this.onLimitReached = listener
    }

    fun setOnLessThanLimitReached(listener: () -> Unit) {
        this.onLessThanLimitReached = listener
    }

    fun setOnMultiSelectionChanged(listener: (List<VideoItem>) -> Unit) {
        this.onMultiSelectionChanged = listener
    }
    inner class VideoViewHolder(override val binding: ItemVideoBinding) : BaseViewHolder<VideoItem, ItemVideoBinding>(binding) {

        init {
            binding.root.tap {
                if (bindingAdapterPosition != NO_POSITION) {
                    data?.let { toggleSelection(it) }
                }
            }
        }

        override fun onBindData(data: VideoItem) {
            super.onBindData(data)

            // Thumbnail
            binding.ivThumbnail.loadImage(data.uri, R.drawable.banner_audio)

            // Duration
            binding.tvDuration.text = data.duration.formatDuration()

            // Selection indicator - similar to MediaFileAdapter
            binding.tvSelectionIndicator.apply {
                visibility = View.VISIBLE
                if (data.selectionOrder > 0) {
                    text = data.selectionOrder.toString()
                    background = ContextCompat.getDrawable(
                        itemView.context, R.drawable.bg_circle_cut
                    )
                } else {
                    text = ""
                    background = null
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoViewHolder(binding)
    }

    fun toggleSelection(item: VideoItem) {
        val existingSelectedItem = selectedItems.find { it.uri == item.uri }

        if (existingSelectedItem != null) {
            // Remove from selection - unselect
            selectedItems.remove(existingSelectedItem)
            renumberSelectedItems()
        } else {
            // Add to selection
            if (selectedItems.size >= MAX_SELECTION_COUNT) {
                onLimitReached()
                return
            }

            if (item.duration < MIN_SELECTED_MEDIA_DURATION_MS) {
                onLessThanLimitReached()
                return
            }

            selectedItems.add(item)
        }

        // Create new list with updated selectionOrder
        val updatedList = currentList.map { currentItem ->
            val selectedItem = selectedItems.find { it.uri == currentItem.uri }
            if (selectedItem != null) {
                val selectionIndex = selectedItems.indexOf(selectedItem) + 1
                currentItem.copy(selectionOrder = selectionIndex)
            } else {
                currentItem.copy(selectionOrder = 0)
            }
        }

        submitList(updatedList)
        onMultiSelectionChanged(selectedItems)
    }

    private fun renumberSelectedItems() {
        selectedItems.forEachIndexed { index, item ->
            item.selectionOrder = index + 1
        }
    }

    fun updateSelection(updated: List<VideoItem>) {
        selectedItems.clear()
        selectedItems.addAll(updated)

        // Create new list with updated selectionOrder
        val updatedList = currentList.map { currentItem ->
            val selectedItem = updated.find { it.uri == currentItem.uri }
            if (selectedItem != null) {
                val selectionIndex = updated.indexOf(selectedItem) + 1
                currentItem.copy(selectionOrder = selectionIndex)
            } else {
                currentItem.copy(selectionOrder = 0)
            }
        }

        submitList(updatedList)
        onMultiSelectionChanged(selectedItems)
    }

    fun getSelectedItems(): List<VideoItem> = selectedItems.toList()

    fun clearSelection() {
        selectedItems.clear()

        // Create new list with selectionOrder reset to 0
        val updatedList = currentList.map { it.copy(selectionOrder = 0) }

        submitList(updatedList)
        onMultiSelectionChanged(emptyList())
    }


    class DiffCallback : DiffUtil.ItemCallback<VideoItem>() {
        override fun areItemsTheSame(oldItem: VideoItem, newItem: VideoItem) =
            oldItem.uri == newItem.uri

        override fun areContentsTheSame(oldItem: VideoItem, newItem: VideoItem) =
            oldItem.uri == newItem.uri &&
                    oldItem.selectionOrder == newItem.selectionOrder &&
                    oldItem.name == newItem.name &&
                    oldItem.size == newItem.size
    }
}