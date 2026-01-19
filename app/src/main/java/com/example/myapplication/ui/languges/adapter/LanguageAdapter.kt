package com.example.myapplication.ui.languges.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.model.LanguageModel
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.tap

class LanguageAdapter(
    private val context: Context,
    private var list: List<LanguageModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<LanguageAdapter.LanguageHolder>() {

    var selectedItemPosition: Int = -1
    private var previousSelectedItemPosition: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LanguageHolder {
        return LanguageHolder(
            LayoutInflater.from(context).inflate(R.layout.item_languages, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LanguageHolder, position: Int) {
        val data = list[position]
        holder.ivLanguage.setImageResource(data.image)
        holder.tvLanguage.text = data.languageName
        if (position == selectedItemPosition) {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_language_on)
            holder.tvLanguage.setTextColor(Color.WHITE)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.bg_item_language_off)
            holder.tvLanguage.setTextColor(Color.BLACK)
        }

        holder.itemView.tap {
            // Lưu vị trí item trước đó
            previousSelectedItemPosition = selectedItemPosition
            // Cập nhật vị trí item mới
            selectedItemPosition = holder.adapterPosition
            // Thông báo thay đổi cho item trước đó và item mới
            notifyItemChanged(previousSelectedItemPosition)
            notifyItemChanged(selectedItemPosition)
            itemClickListener.onItemClick(position)
            SystemUtil.setLocale(context)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class LanguageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivLanguage: ImageView = itemView.findViewById(R.id.iv_language)
        val tvLanguage: TextView = itemView.findViewById(R.id.tv_language)
    }


    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}