package com.example.myapplication.ui.generate

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.model.Note
import com.example.myapplication.data.remote.ApiService
import com.example.myapplication.data.remote.Retrofiter
import com.example.myapplication.databinding.ActivityGenerateBinding
import com.google.android.material.appbar.MaterialToolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class GenerateActivity : BaseActivity<ActivityGenerateBinding>(
    inflater = ActivityGenerateBinding::inflate
) {

    private lateinit var noteAdapter: NoteAdapter
    private val client: ApiService by lazy { Retrofiter.retrofit } // giả sử Retrofiter là object chứa Retrofit instance

    override fun initView() {
        super.initView()

        // Setup Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // nút back nếu cần
        supportActionBar?.title = "Danh sách ghi chú"

        // Setup RecyclerView
        binding.recyclerViewNotes.layoutManager = LinearLayoutManager(this)
        noteAdapter = NoteAdapter(emptyList()) // khởi tạo với list rỗng
        binding.recyclerViewNotes.adapter = noteAdapter

        // Load dữ liệu từ API
        fetchAllNote()
    }

    private fun fetchAllNote() {
        client.getAllNote()
        client.getAllNote().enqueue(object : Callback<List<Note>> {
            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                if (response.isSuccessful) {
                    val notes = response.body() ?: emptyList()

                    // Cập nhật adapter
                    noteAdapter = NoteAdapter(notes)
                    binding.recyclerViewNotes.adapter = noteAdapter

                    // Hiển thị thông báo rỗng nếu cần
                    if (notes.isEmpty()) {
                        Toast.makeText(this@GenerateActivity, "Chưa có ghi chú nào", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@GenerateActivity, "Tải thành công ${notes.size} ghi chú", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(
                        this@GenerateActivity,
                        "Lỗi: ${response.code()} - ${response.message()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                Toast.makeText(this@GenerateActivity, "Lỗi mạng: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Xử lý nút back trên toolbar
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}