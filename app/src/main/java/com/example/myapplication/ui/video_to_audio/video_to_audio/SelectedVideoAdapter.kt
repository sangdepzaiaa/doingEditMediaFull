package com.example.myapplication.ui.video_to_audio.video_to_audio

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemSelectedVideoThumbBinding
import com.example.myapplication.utils.loadImage
import com.example.myapplication.utils.tap

class SelectedVideoAdapter() : RecyclerView.Adapter<SelectedVideoAdapter.ViewHolder>() {

    private val items = mutableListOf<VideoItem>()
    private var onRemoveClick: (VideoItem) -> Unit={}

    fun setOnRemoveClick(onRemoveClick: (VideoItem) -> Unit) {
        // Assign the provided listener to the class property
        this.onRemoveClick = onRemoveClick
    }

    inner class ViewHolder(private val binding: ItemSelectedVideoThumbBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.removeBtn.tap {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION && position < items.size) {
                    onRemoveClick(items[position])
                }
            }
        }

        fun bind(data: VideoItem) {
            binding.thumb.loadImage(data.uri, R.drawable.banner_audio)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectedVideoThumbBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newList: List<VideoItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateList(newList: List<VideoItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }
}