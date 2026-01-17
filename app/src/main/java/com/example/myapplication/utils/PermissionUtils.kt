package com.example.myapplication.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import com.example.myapplication.R
import com.example.myapplication.data.enumm.PermissionType
import com.example.myapplication.databinding.DialogAllowSystemSettingBinding

object PermissionUtils {

    const val PERMISSION_REQUEST_CODE = 1001
    const val WRITE_SETTINGS_REQUEST_CODE = 1002

    fun getRequiredPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun hasBasicPermissions(context: Context): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun canWriteSettings(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }

    fun requestBasicPermissions(activity: Activity) {
        val permissions = getRequiredPermissions()
        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    fun requestWriteSettingsPermission(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = "package:${context.packageName}".toUri()
            }
            context.startActivity(intent)
            context.showToast(context.getString(R.string.please_enable_modify_system_settings_and_return_to_the_app))
        } catch (e: Exception) {
            context.showToast(context.getString(R.string.cannot_open_settings))
        }
    }

    fun showPermissionDialog(
        context: Context,
        type: String,
        onPositive: () -> Unit,
    ) {
        val toast = when (type) {
            PermissionType.RINGTONES.value ->
                context.getString(R.string.permission_required_to_set_ringtones)

            PermissionType.NOTIFICATIONS.value ->
                context.getString(R.string.permission_required_to_set_notifications)

            PermissionType.ALARMS.value ->
                context.getString(R.string.permission_required_to_set_alarms)

            PermissionType.CONTACTS.value ->
                context.getString(R.string.permission_required_to_set_contact)

            else ->
                context.getString(R.string.permission_required_to_set_ringtones)
        }
        val layoutInflater = LayoutInflater.from(context)
        val dialogBinding = DialogAllowSystemSettingBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialogBinding.apply {
            btnSave.setOnClickListener {
                dialog.dismiss()
                onPositive()
            }
            btnCancel.setOnClickListener {
                dialog.dismiss()
                context.showToast(toast)
            }
        }

        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        dialog.window?.setLayout(
            (context.resources.displayMetrics.widthPixels * 0.8).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    fun getContactsPermissions(): List<String> {
        return listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS
        )
    }

    fun hasContactsPermissions(context: Context): Boolean {
        return getContactsPermissions().all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestContactsPermissions(activity: Activity) {
        val permissions = getContactsPermissions()
        ActivityCompat.requestPermissions(
            activity,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }

    fun checkAndRequestAllPermissions(
        activity: Activity,
        type: String,
        onAllGranted: () -> Unit
    ) {
        // Check basic media permissions first
        if (!hasBasicPermissions(activity)) {
            requestBasicPermissions(activity)
            return
        }

        // Check CONTACTS permissions if needed
        if (type == PermissionType.CONTACTS.value && !hasContactsPermissions(activity)) {
            showPermissionDialog(activity, type) {
                requestContactsPermissions(activity)
            }
            return
        }

        // Check WRITE_SETTINGS permission
        if (!canWriteSettings(activity)) {
            showPermissionDialog(activity, type) {
                requestWriteSettingsPermission(activity)
            }
            return
        }

        // All permissions granted
        onAllGranted()
    }

    fun handlePermissionResult(
        context: Context,
        requestCode: Int,
        grantResults: IntArray
    ): Boolean {
        return when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val allGranted = grantResults.isNotEmpty() &&
                        grantResults.all { it == PackageManager.PERMISSION_GRANTED }

                if (allGranted) {
                    context.showToast(context.getString(R.string.permissions_granted))
                } else {
                    context.showToast(context.getString(R.string.permissions_denied))
                }
                allGranted
            }

            else -> false
        }
    }
}