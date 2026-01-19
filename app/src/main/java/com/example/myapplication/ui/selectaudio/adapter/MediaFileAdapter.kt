package com.example.myapplication.ui.selectaudio.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.SelectionMode
import com.example.myapplication.databinding.ItemMediaFileBinding
import com.example.myapplication.utils.const.MAX_SELECTION_COUNT
import com.example.myapplication.utils.const.MIN_SELECTED_MEDIA_DURATION_MS
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.tap

class MediaFileAdapter() : RecyclerView.Adapter<MediaFileAdapter.MediaFileViewHolder>() {

    private val items = mutableListOf<MediaFile>()
    private val selectedItems = mutableListOf<MediaFile>()
    private var onSingleItemSelected: (MediaFile) -> Unit = {}

    private var onMultiSelectionChanged: (List<MediaFile>) -> Unit = {}
    private var onLimitReached: () -> Unit = {}
    private var onLessThanLimitReached: () -> Unit = {}
    private var selectionMode:SelectionMode = SelectionMode.SINGLE
    private lateinit var editType: EditType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaFileViewHolder {
        val binding = ItemMediaFileBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MediaFileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaFileViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<MediaFile>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
    fun setSelectionMode(mode: SelectionMode) {
        this.selectionMode = mode
        if (mode == SelectionMode.SINGLE) {
            selectedItems.clear()
            items.forEach { it.selectionOrder = 0 }
            notifyDataSetChanged()
        }
    }

    fun setEditType(editType: EditType) {
        this.editType = editType
    }
    fun updateSelection(updated: List<MediaFile>) {
        items.replaceAll { it.copy(selectionOrder = 0) }

        updated.forEachIndexed { i, item ->
            val index = items.indexOfFirst { it.id == item.id }
            if (index != -1) {
                items[index] = items[index].copy(selectionOrder = i + 1)
            }
        }

        selectedItems.clear()
        selectedItems.addAll(items.filter { it.selectionOrder > 0 })

        notifyDataSetChanged()
        onMultiSelectionChanged(selectedItems)
    }


    fun setItems(newItems: List<MediaFile>) {
        items.clear()
        items.addAll(newItems)
    }

    fun setOnSingleItemSelected(listener: (MediaFile) -> Unit) {
        this.onSingleItemSelected = listener
    }

    fun setOnMultiSelectionChanged(listener: (List<MediaFile>) -> Unit) {
        this.onMultiSelectionChanged = listener
    }

    fun setOnLimitReached(listener: () -> Unit) {
        this.onLimitReached = listener
    }

    fun setOnLessThanLimitReached(listener: () -> Unit) {
        this.onLessThanLimitReached = listener
    }
    fun getSelectedItems(): List<MediaFile> = selectedItems

    fun getAllItems(): List<MediaFile> = items

    fun clearSelection() {
        selectedItems.forEach { it.selectionOrder = 0 }
        selectedItems.clear()
        onMultiSelectionChanged(emptyList())
        notifyDataSetChanged()
    }

    private fun handleItemClick(position: Int) {
        val item = items[position]
        if (selectionMode == SelectionMode.SINGLE) {
            onSingleItemSelected(item)
        } else {
            toggleSelection(item)
        }
    }

    private fun toggleSelection(item: MediaFile) {
        if (selectedItems.contains(item)) {
            item.selectionOrder = 0
            selectedItems.remove(item)
            renumberSelectedItems()
        } else {
            if (selectedItems.size >= MAX_SELECTION_COUNT) {
                onLimitReached()
                return
            }

            if (item.duration < MIN_SELECTED_MEDIA_DURATION_MS && editType != EditType.AUDIO_MERGER) {
                onLessThanLimitReached()
                return
            }

            selectedItems.add(item)
            item.selectionOrder = selectedItems.size
        }
        onMultiSelectionChanged(selectedItems)
        notifyDataSetChanged()
    }

    private fun renumberSelectedItems() {
        selectedItems.forEachIndexed { index, mediaFile ->
            mediaFile.selectionOrder = index + 1
        }
    }

    inner class MediaFileViewHolder(
        private val binding: ItemMediaFileBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.tap {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                    handleItemClick(bindingAdapterPosition)
                }
            }
        }

        fun bind(data: MediaFile) {
            binding.tvFileName.text = data.name
            binding.ivIcon.setImageResource(R.drawable.music)

            val durationStr = data.duration.formatDuration()
            val sizeStr = data.size.formatFileSize()
            binding.tvTimeFile.text = durationStr
            binding.tvCapacityFile.text = sizeStr
            binding.tvMineTypeFile.text =
                data.format?.value?.uppercase() ?: binding.tvMineTypeFile.context.getString(R.string.unknown)

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

    }
}
