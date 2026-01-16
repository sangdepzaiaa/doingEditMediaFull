package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.SystemClock
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.Keep
import androidx.core.content.FileProvider
import androidx.core.os.BundleCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.local.dao.history.MediaEntity
import com.example.myapplication.databinding.LayoutCustomToastBinding
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.compareTo
import kotlin.io.copyTo
import kotlin.random.Random

fun View.tap(interval: Long = 1000L, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < interval) return@setOnClickListener
        lastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }
}

fun Uri.copyToCacheFile(context: Context, filename: String): File? {
    return try {
        val file = File(context.cacheDir, filename)
        context.contentResolver.openInputStream(this)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun ImageView.loadImage(uri: Uri?, placeholder: Int? = null) {
    val glideRequest = Glide.with(this.context)
        .load(uri)
        .apply(RequestOptions().centerCrop())

    placeholder?.let {
        glideRequest.placeholder(it)
    }

    glideRequest.into(this)
}

@SuppressLint("DefaultLocale")
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private var lastShownTime = 0L

fun Context.showToast(message: String, minInterval: Long = 700) {
    val currentTime = System.currentTimeMillis()
    val binding = LayoutCustomToastBinding.inflate(LayoutInflater.from(this))
    binding.tvMessage.text = message

    if (currentTime - lastShownTime > minInterval) {
        Toast(this).apply {
            duration = Toast.LENGTH_SHORT
            view = binding.root
            val yOffset = (100 * resources.displayMetrics.density).toInt()
            setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, yOffset)
        }.show()
        lastShownTime = currentTime
    }
}

@SuppressLint("DefaultLocale")
fun Long.formatFileSize(): String {
    val kb = this / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    val tb = gb / 1024.0
    return when {
        tb >= 1 -> String.format("%.1f TB", tb)
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$this B"
    }
}

fun generateOutputFileName(
    nameType: String = "audio",
    format: FormatType,
    useRandomCode: Boolean = true
): String {
    val dateFormat = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
    val currentDate = dateFormat.format(Date())
    val extension = format.value

    val baseLength =
        nameType.length + currentDate.length + extension.length + 2 // for dots and underscore
    val randomCodeLength = if (useRandomCode) 7 else 0 // 6 digits + underscore
    val totalLength = baseLength + randomCodeLength

    val adjustedNameType = if (totalLength > 50) {
        val maxNameTypeLength =
            50 - (currentDate.length + extension.length + randomCodeLength + 2)
        nameType.take(maxNameTypeLength.coerceAtLeast(1))
    } else {
        nameType
    }

    return if (useRandomCode) {
        val randomCode = Random.nextInt(100000, 999999)
        "${adjustedNameType}_${currentDate}_$randomCode.$extension"
    } else {
        "${adjustedNameType}_${currentDate}.$extension"
    }
}

fun MediaFile.toMediaEntry(): MediaEntity {
    return MediaEntity(
        name = name.substringBeforeLast("."),
        duration = duration,
        size = size,
        dateAdded = dateAdded,
        path = uri.toString(),
        format = format,
        mediaType = mediaType,
        editType = editType,
        isHistory = true
    )
}

@Keep
fun <T : Parcelable> Intent.getParcelableExtraCompat(key: String, clazz: Class<T>): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableExtra(key, clazz)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableExtra(key) as? T
    }
}

@SuppressLint("QueryPermissionsNeeded")
fun MediaFile.share(context: Context) {
    // Nếu uri là content:// -> copy sang cache
    val file = if (uri.scheme == "content") {
        uriToFile(context, uri, name)
    } else {
        File(uri.path ?: return)
    }
    if (file == null || !file.exists()) {
        Toast.makeText(context, "File not found", Toast.LENGTH_SHORT).show()
        return
    }

    // Mime type theo MediaType
    val mimeType = when (mediaType) {
        MediaType.AUDIO -> "audio/*"
        MediaType.VIDEO -> "video/*"
        else -> "*/*"
    }

    // Convert sang FileProvider URI
    val shareUri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, shareUri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    // Grant permission cho tất cả app nhận share
    val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    for (resolveInfo in resInfoList) {
        context.grantUriPermission(
            resolveInfo.activityInfo.packageName,
            shareUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }

    context.startActivity(Intent.createChooser(intent, "Share via"))
}

fun uriToFile(context: Context, uri: Uri, fileName: String): File? {
    return try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        if (inputStream != null) {
            val tempFile = File(context.cacheDir, fileName)
            FileOutputStream(tempFile).use { output ->
                inputStream.copyTo(output)
            }
            inputStream.close()
            tempFile
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun Context.deleteFile(uri: android.net.Uri): Boolean {
    return try {
        run {
            when (uri.scheme) {
                "content" -> {
                    // Kiểm tra quyền trước khi xóa
                    if (checkUriPermission(
                            uri,
                            android.os.Process.myPid(),
                            android.os.Process.myUid(),
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        )
                        == PackageManager.PERMISSION_GRANTED
                    ) {

                        contentResolver.delete(uri, null, null) > 0
                    } else {
                        // Thử xóa bằng MediaStore
                        deleteFromMediaStore(uri)
                    }
                }

                "file" -> {
                    uri.path?.let { path ->
                        val file = java.io.File(path)
                        file.exists() && file.delete()
                    } ?: false
                }

                else -> false
            }
        }
    } catch (e: Exception) {
        Log.e("DeleteFile", "Error deleting file: ${e.message}", e)
        false
    }


}

fun Context.deleteFromMediaStore(uri: android.net.Uri): Boolean {
    return try {
        // Lấy file path từ MediaStore
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val dataIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                if (dataIndex != -1) {
                    val filePath = cursor.getString(dataIndex)
                    val file = java.io.File(filePath)

                    // Xóa file vật lý trước
                    val fileDeleted = if (file.exists()) file.delete() else true

                    // Sau đó xóa khỏi MediaStore
                    val mediaDeleted = contentResolver.delete(uri, null, null) > 0

                    return fileDeleted || mediaDeleted
                }
            }
        }
        false
    } catch (e: Exception) {
        Log.e("DeleteFile", "Error in deleteFromMediaStore", e)
        false
    }
}

@Keep
inline fun <reified T : Parcelable> Intent.getParcelableArrayListCompat(key: String): ArrayList<T>? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.getParcelableArrayListExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        this.getParcelableArrayListExtra(key)
    }
}

@Keep
inline fun <reified T : Parcelable> Bundle.getParcel(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        BundleCompat.getParcelable(this, key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelable(key)
    }
}

inline fun <reified T : java.io.Serializable> Intent.getSerializableCompat(key: String): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getSerializableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getSerializableExtra(key) as? T
    }
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun generateOutputFile(
    nameType: String = "volume",
    outputDir: File,
    extension: String = "mp3"
): File {
    var outputFile: File
    do {
        val fileName =
            generateOutputFileName(
                nameType,
                FormatType.fromValue(extension)
            )
        outputFile = File(outputDir, fileName)
    } while (outputFile.exists())

    return outputFile
}


//Copy dữ liệu từ URI sang file
//context.contentResolver :  làm việc với URI (content://...)
// có thể:
//mở file từ Gallery
//đọc ảnh từ MediaStore
//đọc PDF, video, audio
//truy cập dữ liệu ứng dụng khác chia sẻ
//Nó giống như "cây cầu" → lấy dữ liệu từ một URI.

//openInputStream(uri) :MỞ URI + ĐỌC/LẤY DỮ LIỆU BÊN TRONG URI
//openInputStream() trả về null, phần bên trong sẽ không chạy.
// truyền this do tính năng của extension function, this là CLASS  mà nó mở rộng, ở đây this là Uri

//use : tự động đóng luồng sau khi hoàn thành công việc với luồng đó

//FileOutputStream(file): MỞ FILE THẬT + CHUẨN BỊ CHO GHI DỮ LIỆU VÀO

//copyTo : đọc từng chunk byte từ input,ghi sang output,lặp cho đến khi hết dữ liệu

//openInputStream đọc dữ liệu từ URI, FileOutputStream mở file để ghi, copyTo đổ toàn bộ dữ liệu từ URI vào file.

