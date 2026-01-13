package com.example.myapplication.ui.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.local.dao.AppDatabase
import com.example.myapplication.data.model.ImageEntity
import com.example.myapplication.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.File

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val imageDao = db.imageDao()

    val allImages = imageDao.getAllImages().asLiveData()
    val postResultBitmap = MutableLiveData<Bitmap>()
    val statusMessage = MutableLiveData<String>()

    fun syncDataFromApi() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.instance.getListItems()
                val entities = response.map { ImageEntity(it.id, it.title, it.description, it.image_url) }
                imageDao.syncImages(entities)
                statusMessage.postValue("Đã đồng bộ dữ liệu mới nhất.")

                // Sau khi lưu, log toàn bộ bảng
                val all = imageDao.getAllImages().first()
                //   lấy snapshot từ Flow
                all.forEach { img ->
                    Log.d("RoomTable",
                        "ID=${img.id}, Title=${img.title}, Desc=${img.description}, URL=${img.image_url}") }
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun uploadAndSync(uri: Uri, title: String, desc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())

                val responseBody = RetrofitClient.instance.uploadData(titlePart, descPart, imagePart)
                val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
                postResultBitmap.postValue(bitmap)

                syncDataFromApi()
                statusMessage.postValue("Upload thành công!")
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    // ------------------ UPDATE ------------------
    fun updateImage(item: ImageEntity, uri: Uri?, newTitle: String, newDesc: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val titlePart = newTitle.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = newDesc.toRequestBody("text/plain".toMediaTypeOrNull())
                val filePart = uri?.let { val file = uriToFile(it)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("file", file.name, requestFile)
                }
                RetrofitClient.instance.updateItem(item.id, titlePart, descPart, filePart)
                syncDataFromApi()
                statusMessage.postValue("Cập nhật thành công!")
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }
    // ------------------ DELETE ------------------
     fun deleteImage(item: ImageEntity) {
         viewModelScope.launch(Dispatchers.IO) {
             try {
                 RetrofitClient.instance.deleteItem(item.id)
                 syncDataFromApi()
                 statusMessage.postValue("Xóa thành công!")
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
