package com.example.myapplication.ui.home

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.example.myapplication.data.local.dao.AppDatabase
import com.example.myapplication.data.model.ImageEntity
import com.example.myapplication.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class ImageViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val imageDao = db.imageDao()


    // UI sẽ lắng nghe biến này. Cứ Room có biến động là UI tự vẽ lại.
    val allImages = imageDao.getAllImages().asLiveData()

    val postResultBitmap = MutableLiveData<Bitmap>()
    val statusMessage = MutableLiveData<String>()

    // --- CHỨC NĂNG READ (Lấy từ API -> Đổ vào Room) ---
    fun syncDataFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getListItems()
                val entities = response.map { ImageEntity(it.id, it.title, it.description, it.image_url) }

                // Cập nhật DB bằng transaction, tránh spam
                imageDao.syncImages(entities)

                statusMessage.postValue("Đã đồng bộ dữ liệu mới nhất.")
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }


    // --- CHỨC NĂNG CREATE (Gửi lên API -> Thành công thì cập nhật Room) ---
    fun uploadAndSync(uri: Uri, title: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())

                // 1. Post lên server
                val responseBody = RetrofitClient.instance.uploadData(titlePart, descPart, imagePart)

                // 2. Nhận ảnh kết quả hiển thị lên UI tạm thời
                val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
                postResultBitmap.postValue(bitmap)

                // 3. QUAN TRỌNG: Gọi lại hàm sync để Room cập nhật danh sách mới từ Server
                syncDataFromApi()

                statusMessage.postValue("Upload thành công!")
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        if (e is HttpException) {
            statusMessage.postValue("Lỗi Server: ${e.code()}")
        } else {
            statusMessage.postValue("Lỗi kết nối: Check IP Server!")
        }
    }

    private fun uriToFile(uri: Uri): File {
        val context = getApplication<Application>().applicationContext
        val file = File(context.cacheDir, "temp_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file
    }
}