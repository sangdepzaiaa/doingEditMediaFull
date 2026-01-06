package com.example.myapplication.ui.generate

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.data.model.Note
import java.io.File

class NoteAdapter(private val notes: List<Note>) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgNote: ImageView = itemView.findViewById(R.id.imgNote)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]

        holder.tvTitle.text = note.title
        holder.tvDescription.text = note.description

        // Load ảnh bằng Glide
        // Nếu imgPath là đường dẫn file local:
        // Glide.with(holder.itemView.context).load(File(note.imgPath)).into(holder.imgNote)

        // Nếu imgPath là URL (http/https):
        Glide.with(holder.itemView.context)
            .load(note.imgPath)  // có thể là String URL hoặc File
            .placeholder(R.drawable.img_banner)  // optional: ảnh placeholder trong lúc load
            .error(R.drawable.ic_close)              // optional: ảnh lỗi nếu load thất bại
            .centerCrop()
            .into(holder.imgNote)
    }

    override fun getItemCount(): Int = notes.size
}