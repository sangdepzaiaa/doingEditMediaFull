package com.example.myapplication.ui.permission

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityPermissionBinding
import com.example.myapplication.ui.home.HomeActivity
import com.example.myapplication.utils.tap
import io.ktor.client.plugins.UserAgent

class PermissionActivity : BaseActivity<ActivityPermissionBinding>(
    inflater = ActivityPermissionBinding::inflate
) {
    val laucher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        setSwitchState(binding.swCamera,hasPermission())
        updateText()
        if (isGranted) {
            Toast.makeText(this, R.string.grant_permission_camera, Toast.LENGTH_SHORT).show()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showDetailDialog()
            } else {
                showSettingDialog()
            }
        }
    }

    fun showDetailDialog() {
        AlertDialog.Builder(this).setTitle(R.string.permission)
            .setMessage(R.string.grant_permission).setCancelable(true)
            .setPositiveButton(R.string.grant_permission) { _, _ ->
                laucher.launch(Manifest.permission.CAMERA)
            }.setNegativeButton(R.string.cancel) { dia, _ ->
                dia.dismiss()
            }.show()
    }

    fun showSettingDialog() {
        AlertDialog.Builder(this).setTitle(R.string.permission).setTitle(R.string.grant_permission)
            .setCancelable(true).setPositiveButton(R.string.grant_permission) { _, _ ->
                showDetailSetting()
            }.setNegativeButton(R.string.cancel) { dia, _ ->
                dia.dismiss()
            }.show()
    }

    fun showDetailSetting() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            startActivity(
                Intent(Settings.ACTION_APPLICATION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            )
        }

    }

    fun hasPermission(): Boolean =
        ContextCompat
            .checkSelfPermission(this@PermissionActivity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun requestPermission() {
        if (hasPermission()){
            setSwitchState(binding.swCamera,hasPermission())
            updateText()
            return
        }
        laucher.launch(Manifest.permission.CAMERA)
    }

    override fun initView() {
        super.initView()
        if (hasPermission()){
            negativeHome()
            return
        }

        setSwitchState(binding.swCamera,hasPermission())
        updateText()
        setOnClick()
    }

    fun negativeHome(){
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    fun setOnClick(){
        binding.apply {
            icDone.tap { negativeHome() }
            tvConfirm.tap { negativeHome() }
            tvCamera.tap { requestPermission() }
            swCamera.tap { requestPermission() }
        }
    }

    fun setSwitchState(switchCompat: SwitchCompat,  isGranteded: Boolean){
        switchCompat.isEnabled = isGranteded

        if (isGranteded){
            val thumbOn = ContextCompat.getColor(this@PermissionActivity,R.color.color_00C40D)
            val trackOn = ContextCompat.getColor(this@PermissionActivity,R.color.color_000000_7)

            binding.swCamera.thumbTintList = ColorStateList.valueOf(thumbOn)
            binding.swCamera.trackTintList = ColorStateList.valueOf(trackOn)
            binding.swCamera.isEnabled = false
        }else{
            binding.swCamera.isEnabled = true
            val thumbOff = ContextCompat.getColor(this@PermissionActivity,R.color.color_1B1B1B_7)
            val trackOff = ContextCompat.getColor(this@PermissionActivity,R.color.color_000000_7)

            binding.swCamera.trackTintList = ColorStateList.valueOf(trackOff)
            binding.swCamera.thumbTintList = ColorStateList.valueOf(thumbOff)

        }

    }


    fun updateText() {
        val isGrant = hasPermission()
        binding.icDone.visibility  = if (isGrant) {
            android.view.View.VISIBLE
        } else {
            android.view.View.INVISIBLE
        }
        binding.tvConfirm.apply {
            if (isGrant) {
                text = getString(R.string.continue_n)
            }else{
                text = getString(R.string.continue_without_permission)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateText()
        setSwitchState(binding.swCamera,hasPermission())
    }
}


// registerForActivityResult( // đăng ký launch yêu cầu quyền
//        ActivityResultContracts.RequestPermission() // yêu cầu 1 quyền
// isGranted = true : đã cấp , = false : chưa cấp
// shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) : deny permission nhưng chưa chọn "không hỏi lại"
//  launcherCamera.launch(android.Manifest.permission.CAMERA) .launch : thực hiện yêu cầu cấp quyeenf cụ thể
//  Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) mở cài đặt ứng dụng để người dùng tự cấp quyền
//    data = Uri.fromParts("package",packageName,null) : Tạo một cái URI đặc biệt có dạng: package:com.example.myapp
// "package" Phần đầu của URI (trước dấu :) Nói với Android: “Đây là URI kiểu package nha, không phải http hay content đâu”
// packageName (ví dụ: "com.zalo") Phần sau dấu : Tên gói ứng dụng thực tế mà mày muốn mở cài đặt
//null Phần cuối cùng, thường dùng cho các tham số bổ sung, nhưng ở đây không cần nên để null
//Intent(Settings.ACTION_APPLICATION_SETTINGS) : mở cài đặt chung của thiết bị
//  addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) : mở cài đặt trong task mới
//  addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY) : không lưu hoạt động cài đặt trong lịch sử
//  addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) : không hiển thị trong ứng dụng gần đây
// Intent.FLAG_ACTIVITY_CLEAR_TASK : xóa tất cả hoạt động hiện tại trong task trước khi bắt đầu hoạt động mới
//checkSelfPermission :
// PackageManager.PERMISSION_GRANTED : hằng số biểu thị quyền đã được cấp
// switchCompat.isChecked = isGranted : đặt trạng thái on/off của switch dựa trên quyền
//switchCompat.thumbTintList = ColorStateList.valueOf(thumbOn) : set màu cho thumb khi quyền được cấp
//switchCompat.trackTintList = ColorStateList.valueOf(trackOn): set màu cho track khi quyền được cấp
//switchCompat.isEnabled = false : vô hiệu hóa switch khi quyền đã được cấp
//binding.icDone.isVisible = isGranted  : hiển thị nút done nếu quyền đã được cấp
//binding.tvConfirm.text = if(isGranted){ : đặt text cho nút xác nhận dựa trên trạng thái quyền
//    getString(R.string.continue_n) : nếu có quyền, hiển thị "Continue"
//  finishAffinity() : kết thúc tất cả các hoạt động trong cùng một tác vụ và thoát ứng dụng hiện tại


