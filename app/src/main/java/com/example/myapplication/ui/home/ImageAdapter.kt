package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.ItemResponse
import com.example.myapplication.databinding.ItemImageBinding



import com.bumptech.glide.Glide
import com.example.myapplication.data.model.ImageEntity


// Chú ý: ImageEntity là Model của Room
class ImageAdapter(private val onItemClick: (ImageEntity) -> Unit) :
    ListAdapter<ImageEntity, ImageAdapter.ImageViewHolder>(DiffCallback) {

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvFilename.text = item.title
        holder.binding.tvSize.text = item.description
        holder.binding.tvStatus.text = "ID: ${item.id}"

        Glide.with(holder.itemView.context)
            .load(item.image_url)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_report_image)
            .into(holder.binding.imageView)

        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ImageEntity>() {
        override fun areItemsTheSame(oldItem: ImageEntity, newItem: ImageEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ImageEntity, newItem: ImageEntity) = oldItem == newItem
    }
}

