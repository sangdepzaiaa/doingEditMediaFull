package com.example.myapplication.ui.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.ui.home.HomeActivity
import com.example.myapplication.utils.tap
import com.xxx.faceswap.doingeditmediafull.R
import com.xxx.faceswap.doingeditmediafull.databinding.ActivityPermissionBinding

class PermissionActivity: BaseActivity<ActivityPermissionBinding>(
    inflater = ActivityPermissionBinding::inflate
){
    val launchCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        updatePermissionStatus()
        updateDoneBotton()
        if (isGranted) {
            Toast.makeText(this, R.string.grant_permission_camera, Toast.LENGTH_SHORT).show()
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                showPermissionDialog()
            } else {
                showGotoSettingsDialog()
            }
        }
    }

    fun showPermissionDialog(){
        AlertDialog.Builder(this)
            .setTitle(R.string.permission)
            .setMessage(R.string.permission_camera_explanation)
            .setCancelable(true)
            .setPositiveButton(R.string.grant_permission){_,_->
            launchCamera.launch(Manifest.permission.CAMERA)
            }
            .setNegativeButton(R.string.cancel,null)
            .show()
    }

    fun showGotoSettingsDialog(){
        AlertDialog.Builder(this)
            .setTitle(R.string.permission)
            .setMessage(R.string.permission_camera_message)
            .setCancelable(true)
            .setPositiveButton(R.string.grant_permission){_,_->
                goToSeting()
            }
            .setNegativeButton(R.string.cancel,null)
            .show()
    }

    fun goToSeting(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package",packageName,null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        try {
            startActivity(intent)
        }catch (e: Exception){
            val intent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            startActivity(intent)
        }
    }

    fun hasPermissionCamera(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun requestPermissionCamera(){
        if (hasPermissionCamera()){
            updatePermissionStatus()
            updateDoneBotton()
        }
        launchCamera.launch(Manifest.permission.CAMERA)
    }

    override fun initView() {
        super.initView()
        if (hasPermissionCamera()){
            navigateToHome()
            return
        }

        setClick()
        updatePermissionStatus()
        updateDoneBotton()

    }

    fun updatePermissionStatus(){
        updateSwitch(binding.swCamera,hasPermissionCamera())
    }

    fun updateSwitch(switchCompat: SwitchCompat,isGranted: Boolean){
        switchCompat.isEnabled = isGranted

        if (isGranted){
            val thumOn = ContextCompat.getColor(this,R.color.color_00C40D)
            val trackOn = ContextCompat.getColor(this, R.color.white)

            switchCompat.thumbTintList = ColorStateList.valueOf(thumOn)
            switchCompat.trackTintList = ColorStateList.valueOf(trackOn)
            switchCompat.isEnabled = false
        }else{
            switchCompat.isEnabled = true
            val thumbOff = ContextCompat.getColor(this,R.color.black)
            val trackOff = ContextCompat.getColor(this, R.color.white)

            switchCompat.thumbTintList = ColorStateList.valueOf(thumbOff)
            switchCompat.trackTintList = ColorStateList.valueOf(trackOff)
        }
    }

    fun updateDoneBotton(){
        val isGranted = hasPermissionCamera()
        binding.icDone.isVisible = isGranted
        binding.tvConfirm.text = if(isGranted){
            getString(R.string.continue_n)
        }else{
            getString(R.string.continue_without_permission)
        }
    }

    fun navigateToHome(){
        startActivity(Intent(this,HomeActivity::class.java))
        finish()
    }

    fun setClick(){
        binding.apply {
            tvCamera.tap { requestPermissionCamera() }
            swCamera.tap { requestPermissionCamera() }
            tvConfirm.tap { navigateToHome() }
            icDone.tap { navigateToHome() }
        }
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        updateDoneBotton()
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


