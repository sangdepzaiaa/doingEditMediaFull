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
import com.example.myapplication.databinding.ItemLanguagesBinding
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.tap


//position : vị trí của item khi được bind
//adapterPosition : vị trí hiện tại của item trong adapter,
//notifyItemChanged gọi lại onBindViewHolder, vẽ lại ui item đó
//RecyclerView bỏ qua position không hợp lệ : ví dụ -1
class LanguageAdapter(
    private var list: List<LanguageModel>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<LanguageAdapter.LanguageHolder>() {

    var selectedItemPosition: Int = -1
    private var previousSelectedItemPosition: Int = -1
    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): LanguageHolder {
        return LanguageHolder(
            ItemLanguagesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
            //selectedItemPosition = holder.adapterPosition
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@tap
            selectedItemPosition = pos

            // Thông báo thay đổi cho item trước đó và item mới
            if (previousSelectedItemPosition != -1) {
                notifyItemChanged(previousSelectedItemPosition)
            }
            notifyItemChanged(selectedItemPosition)
            itemClickListener.onItemClick(list[selectedItemPosition].code)
            // SystemUtil.setLocale(context)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class LanguageHolder(binding : ItemLanguagesBinding) : RecyclerView.ViewHolder(binding.root) {
        val ivLanguage = binding.ivLanguage
        val tvLanguage = binding.tvLanguage
    }


    interface OnItemClickListener {
        fun onItemClick(languageTag: String)
    }

}