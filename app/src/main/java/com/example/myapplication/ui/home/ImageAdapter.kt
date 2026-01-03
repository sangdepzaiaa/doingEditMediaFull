package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.ItemResponse
import com.example.myapplication.databinding.ItemImageBinding



import com.bumptech.glide.Glide


class ImageAdapter(
    private var images: List<ItemResponse>,
    private val onItemClick: (ItemResponse) -> Unit
) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    class ImageViewHolder(val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ImageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val item = images[position]

        // Map dữ liệu từ API vào UI
        holder.binding.tvFilename.text = item.title
        holder.binding.tvSize.text = item.description // Mô tả ảnh
        holder.binding.tvStatus.text = "ID: ${item.id}"

        // Dùng Glide để tải ảnh từ URL server (ví dụ: http://10.0.2.2:8000/static/abc.jpg)
        Glide.with(holder.itemView.context)
            .load(item.image_url)
            .centerCrop()
            .placeholder(android.R.drawable.ic_menu_report_image) // Ảnh hiện khi đang load
            .into(holder.binding.imageView) // Đảm bảo trong item_image.xml có ImageView này

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int = images.size

    fun updateData(newImages: List<ItemResponse>) {
        images = newImages
        notifyDataSetChanged()
    }
}

