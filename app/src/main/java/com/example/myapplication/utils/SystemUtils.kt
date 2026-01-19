package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.core.net.toUri
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.utils.const.KEY_LANGUAGE
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.Locale
import kotlin.io.copyTo

object SystemUtil {
    private var myLocale: Locale? = null

    private const val KEY_REGION_NAME = "KEY_REGION_NAME"

    // Lưu ngôn ngữ đã cài đặt
    fun saveLocale(context: Context, lang: String?) {
        setPreLanguage(context, lang)
    }

    // Load lại ngôn ngữ đã lưu và thay đổi chúng
    fun setLocale(context: Context) {
        val language = getPreLanguage(context)
        if (language == "") {
            val config = Configuration()
            val locale = Locale.getDefault()
            Locale.setDefault(locale)
            config.locale = locale
            context.resources
                .updateConfiguration(config, context.resources.displayMetrics)
        } else {
            changeLang(language, context)
        }
    }

    // method phục vụ cho việc thay đổi ngôn ngữ.
    fun changeLang(lang: String?, context: Context) {
        if (lang.equals("", ignoreCase = true)) return
        myLocale = Locale(lang)
        saveLocale(context, lang)
        if (myLocale != null) {
            Locale.setDefault(myLocale!!)
        }
        val config = Configuration()
        config.locale = myLocale
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getPreLanguage(mContext: Context): String? {
        val preferences = mContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString(KEY_LANGUAGE, "")
    }

    fun setPreLanguage(context: Context, language: String?) {
        if (language == null || language == "") {
        } else {
            val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
            preferences.edit {
                putString(KEY_LANGUAGE, language)
            }
        }
    }

    fun setActive(context: Context, value: Boolean) {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putBoolean("active", value)
        }
    }

    fun getActive(context: Context, value: Boolean): Boolean {
        val sharedPreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("active", value)
    }

    fun openAppInPlayStore(context: Context) {
        val appPackageName = context.packageName
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // context là Activity hoặc Dialog
            if (context is Activity) {
                context.startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        } catch (e: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=$appPackageName".toUri()
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (context is Activity) {
                context.startActivity(intent)
            } else {
                context.startActivity(intent)
            }
        }
    }

    /**
     * return next activity in common screen
     * config next screen in firebase remote config
     */

    fun getActivityClass(name: String?, defaultClass: Class<*>): Class<*>? {

//        if (name.equals("language"))
//            return LanguageStartActivity::class.java
//        if (name.equals( "intro"))
//            return IntroActivity::class.java
//        if (name.equals("app_update"))
//            return AppUpdateActivity::class.java
//        //home activity- old version - using fragment
//        if (name.equals("home"))
//            return HomeActivity::class.java
//        if (name == "permission")
//            return PermissionActivity::class.java
//        if (name == "bot_setting")
//            return SettingActivity::class.java
        return defaultClass
    }

    fun <T> getConfigObjectFromString(config: String, objectConvert: Class<T>): T? {
        return if (config != "") {
            try {
                Gson().fromJson(config, objectConvert)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            null
        }
    }

    fun saveRegionName(context: Context, regionName: String?) {
        val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        preferences.edit {
            putString(KEY_REGION_NAME, regionName ?: "")
        }
    }

    fun getRegionName(context: Context): String? {
        val preferences = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preferences.getString(KEY_REGION_NAME, "")
    }

    // --- Helper: convert content:// -> File
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

    @SuppressLint("QueryPermissionsNeeded")
    fun shareFileCompat(context: Context, mediaFile: MediaFile) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when (mediaFile.mediaType) {
                MediaType.AUDIO -> "audio/*"
                MediaType.VIDEO -> "video/*"
                else -> "*/*"
            }

            // Lấy Uri an toàn
            val shareUri: Uri = when {
                mediaFile.uri.scheme.equals("content", ignoreCase = true) -> {
                    mediaFile.uri
                }
                mediaFile.uri.scheme.equals("file", ignoreCase = true) -> {
                    val file = File(mediaFile.uri.path!!)
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }
                else -> {
                    // Trường hợp chưa rõ, fallback sang FileProvider
                    val file = File(mediaFile.uri.path ?: mediaFile.name)
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        file
                    )
                }
            }

            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Grant permission cho tất cả app nhận share
        val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            context.grantUriPermission(packageName, mediaFile.uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}
