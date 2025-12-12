package com.example.myapplication.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.FaceDetectionResult
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.ui.dialog.DialogCheckFaceId
import com.example.myapplication.ui.dialog.DialogTypeChoosePhoto
import com.example.myapplication.ui.permission.PermissionActivity
import com.example.myapplication.utils.copyToCacheFile
import com.example.myapplication.utils.tap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
class HomeActivity : BaseActivity<ActivityHomeBinding>(
    inflater = ActivityHomeBinding::inflate
) {
    private var isChoosingFacePhoto = true
    private var currentFaceFile: File? = null   // Ảnh khuôn mặt (từ imgPhoto)
    private var currentStyleFile: File? = null  // Ảnh style (từ imgYourStyle)
    var tempFile: File?=null

    val pickPhotoContents = registerForActivityResult(ActivityResultContracts.GetContent()){uri ->
        uri?.let{ handleUri(it)}
    }

    val pickPhotoVisual = registerForActivityResult(ActivityResultContracts.PickVisualMedia()){uri ->
        uri?.let { handleUri(it)}
    }

    val launchCamara = registerForActivityResult(ActivityResultContracts.TakePicture()){success ->
        if (success){
            tempFile?.let { checkFace(it) }
        }else{
            tempFile?.delete()
            tempFile = null
        }
    }

    var photoBottomSheet : DialogTypeChoosePhoto?=null

    fun showChooseTypePhoto(forFace: Boolean = true){
        isChoosingFacePhoto = forFace
        photoBottomSheet?.dismissAllowingStateLoss()
        photoBottomSheet = null

        photoBottomSheet = DialogTypeChoosePhoto(object : DialogTypeChoosePhoto.OnSelectedListener{
            override fun onPhotoSelected() {
                photoBottomSheet = null
                openGallery()
            }

            override fun onCameraSelected() {
                photoBottomSheet = null
                openCamera()
            }

        })

        if (!isFinishing && !isDestroyed && supportFragmentManager.isStateSaved.not()){
            photoBottomSheet?.show(supportFragmentManager, "PhotoTypeBottomSheet")
        }
    }

    fun openGallery(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            pickPhotoVisual.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }else{
            pickPhotoContents.launch("image/*")
        }
    }

    fun hasPermissionCamera(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun openCamera(){
        if (!hasPermissionCamera()){
            Toast.makeText(this, R.string.permission_camera, Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, PermissionActivity::class.java))
            return
        }
        openCameraInternal()
    }

    fun openCameraInternal(){
        tempFile = File(cacheDir,"camera_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider",tempFile ?: return)
        launchCamara.launch(uri)
    }

    fun handleUri(uri: Uri){
        val file = uri.copyToCacheFile(this, "photo_${System.currentTimeMillis()}.jpg")
        file?.let { checkFace(it) }
    }


    fun checkFace(file: File) {
        if (!file.exists()) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            return
        }
        // Kiểm tra khuôn mặt bằng ML Kit
        detectFaceInImage(file) { result ->
            lifecycleScope.launch {
                when (result) {
                    // 1. OK → khuôn mặt chính diện, rõ ràng → đi tiếp
                    FaceDetectionResult.SingleGoodFace -> {
                        updateImageAndFile(file)
                    }

                    FaceDetectionResult.NoFace -> {
                        DialogCheckFaceId(
                            title = getString(R.string.no_face_detected),
                            content = getString(R.string.please_try_again_with_a_frontal_portrait_photo),
                            action = { showChooseTypePhoto() }
                        ).show(supportFragmentManager, "NoFaceDialog")
                    }

                    FaceDetectionResult.MultipleFaces -> {
                        DialogCheckFaceId(
                            title = getString(R.string.more_than_one_face_detected),
                            content = getString(R.string.please_try_again_with_a_single_faced_frontal_portrait),
                            action = { showChooseTypePhoto() }
                        ).show(supportFragmentManager, "MultiFaceDialog")
                    }

                    FaceDetectionResult.Error -> {
                        DialogCheckFaceId(
                            title = getString(R.string.error),
                            content = getString(R.string.no_face_detected),
                            action = { showChooseTypePhoto() }
                        ).show(supportFragmentManager, "BlurFaceDialog")
                    }
                }
            }
        }
    }

    private fun updateImageAndFile(file: File) {
        val targetImageView = if (isChoosingFacePhoto) binding.imgPhoto else binding.imgYourStyle

        Glide.with(this)
            .load(file)
            .centerCrop()
            .placeholder(R.drawable.img_banner)
            .into(targetImageView)

        if (isChoosingFacePhoto) {
            currentFaceFile = file
        } else {
            currentStyleFile = file
        }

        Toast.makeText(this, "Ảnh đã được chọn thành công!", Toast.LENGTH_SHORT).show()
    }

    private fun detectFaceInImage(file: File, onResult: (FaceDetectionResult) -> Unit) {
        val image = InputImage.fromFilePath(this, Uri.fromFile(file))

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.1f)
                .build()
        )

        detector.process(image)
            .addOnSuccessListener { faces ->
                when {
                    faces.isEmpty() -> {
                        onResult(FaceDetectionResult.NoFace)
                    }
                    faces.size > 1 -> {
                        onResult(FaceDetectionResult.MultipleFaces)
                    }
                    else -> {
                        val face = faces[0]
                        val smileProb = face.smilingProbability ?: 0f
                        val leftEyeOpen = face.leftEyeOpenProbability ?: 0f
                        val rightEyeOpen = face.rightEyeOpenProbability ?: 0f
                        val headEulerY = face.headEulerAngleY // góc quay ngang
                        val headEulerZ = face.headEulerAngleZ // góc nghiêng đầu

                        // Điều kiện "chính diện, rõ ràng"
                        val isFrontal = kotlin.math.abs(headEulerY) < 35f && kotlin.math.abs(headEulerZ) < 30f
                      //  val isEyesOpen = (leftEyeOpen > 0.5f && rightEyeOpen > 0.5f)

                        if (isFrontal ) {
                            onResult(FaceDetectionResult.SingleGoodFace)
                        } else {
                            onResult(FaceDetectionResult.Error)
                        }
                    }
                }
            }
            .addOnFailureListener {
                onResult(FaceDetectionResult.Error)
            }
    }



    override fun onDestroy() {
        photoBottomSheet?.dismissAllowingStateLoss()
        photoBottomSheet = null
        tempFile?.delete()
        currentFaceFile?.delete()
        currentStyleFile?.delete()
        super.onDestroy()
    }

    override fun initView() {
        super.initView()
        binding.apply {
            imgReChoosePhoto.tap { showChooseTypePhoto(forFace = true) }
            imgReChoosePhoto2.tap { showChooseTypePhoto(forFace = false) }
            btnGenerateAiArts.root.tap {
                delayBtnGenerate()
                setUiButton(false)
            }
        }
    }

    private fun setEnabledAiPhoto(isEnabled: Boolean) {
        if (isEnabled) {
            // Trạng thái bình thường: sáng, có thể bấm, không loading
            binding.btnGenerateAiArts.clGenerate.visibility = View.VISIBLE
            binding.btnGenerateAiArts.clLoading.visibility = View.GONE

            binding.btnGenerateAiArts.root.isEnabled = true
            binding.btnGenerateAiArts.root.alpha = 1f
        } else {
            // Đang xử lý: mờ + hiện loading
            binding.btnGenerateAiArts.clGenerate.visibility = View.GONE
            binding.btnGenerateAiArts.clLoading.visibility = View.VISIBLE

            binding.btnGenerateAiArts.root.isEnabled = false
            binding.btnGenerateAiArts.root.alpha = 0.5f
        }
    }

    private fun delayBtnGenerate() {
        binding.btnGenerateAiArts.root.alpha = 0.5f
        binding.btnGenerateAiArts.root.isEnabled = false
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            binding.btnGenerateAiArts.root.alpha = 1f
            binding.btnGenerateAiArts.root.isEnabled = true
        }
    }
    private fun setUiButton(isEnabled: Boolean) {
        setEnabledAiPhoto(isEnabled)
    }
}

//input: uri:  content://com.android.providers.media.documents/document/image%3A94821
// tempFile:  FF D8 FF E0 00 10 4A 46 49 46 00 01 ...
// file được lưu ở :  data/user/0/com.yourapp/cache/temp_chamera_photo.jpg
// hểu sâu hơn: minh họa cụ thể pipeline từ bit → byte → pixel màu để bạn thấy rõ cách máy tính “dịch” dữ liệu
// bit → byte → ByteBuffer trong ImageProxy → Chuyển YUV => RGB →  Bitmap → JPEG(.jpg: FF D8 FF E0 00 10 4A 46 49 46 00 01 ...) : ảnh chụp camera phổ biến -> Uri
//PNG (.png) 89 50 4E 47 0D 0A 1A 0A
//BMP (.bmp) 42 4D ...
// GIF (.gif) 47 49 46 38 39 61
// các dạng khác cũng bắt ầu bằng 2 số
//bit → byte → buffer → ImageProxy → file → URI.
//

//registerForActivityResult : đăng ký một trình khởi chạy(launch) để nhận kết quả từ một hoạt động .
//ActivityResultContracts : hợp động để dùng các dịch cụ của registerForActivityResult

//GetContent() → mở hệ thống file picker (gallery) của thiết bị. android 12 về trước,/trả vee uri tham chieeus đến aảnh
// người dugng chọn ảnh từ gallery, android trả về uri ảnh đã chọn,
// dev viết:  pickPhotoContent.launch("image/*") : chỉ chọn ảnh
// uri trong hàm pickPhotoContent là uri thực sự mà android trả veef khi user chọn ảnh

//PickVisualMedia() mở hệ thống file picker (gallery) của thiết bị. android 13+,trả vee uri tham chieeus đến aảnh
// người dugng chọn ảnh từ gallery, android trả về uri ảnh đã chọn,
// dev viết:
// pickPhotoVisual.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) : chỉ chọn ảnh
//// uri trong hàm pickPhotoVisual là uri thực sự mà android trả veef khi user chọn ảnh
////pickPhotoVisual.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)): mở gallery để chọn ảnh trên android 13+

//TakePicture()
//android làm : thực hiện hành động chụp một bức ảnh và lưu ảnh vào cacheDir trong URI mà dev cung cấp
//dev : tạo file cacheDir truyền vào uri và truyền uri vào launch()
//if (success) { }:  Người dùng đã chụp và bấm lưu.Ảnh chất lượng cao đã nằm sẵn trong file tạm(trong uri).(uri được truyeenf ở launchCamera.launch(uri))
// mở camera để chụp ảnh và trả về true  -> ảnh chụp được lưu thành công vào file bạn cung cấp, let sẽ lấy file tạm và cho vào checkFace,
// scope một hàm generic (phạm vi chung) trong Kotlin, nghĩa là nó không quan tâm đối tượng là kiểu gì, miễn là đối tượng đó tồn tại (không null)
// . Nó hoạt động giống nhau với bất kỳ kiểu dữ liệu nào.
//false -> thất bại và xóa, tempFile?.delete()
//checkSelfPermission kiểm tra xem ứng dụng đã được cấp quyền hay chưa
//PackageManager.PERMISSION_GRANTED : quyền đã được cấp
//File(cacheDir,"camera_${System.currentTimeMillis()}.jpg") tạo file tạm thời trong bộ nhớ đệm của ứng dụng để lưu ảnh chụp từ camera
//FileProvider.getUriForFile(this,"${packageName}.provider",tempFile ?: return) : tạo uri an toàn cho file tạm thời để truyền cho camera
//FileProvider : chia sẻ file cho app khác, getUriForFile : chuyển file thành uri an toàn

//RequestPermission() trả về boolean , hiển thị popup xin quyền,
//isGranted = true → user bấm Allow
//isGranted = false → user bấm Deny hoặc không cho phép
//.dismissAllowingStateLoss()
//→ Tắt/dismiss bottom sheet, cho phép mất state (tránh crash khi activity đang background).
//isFinishing: activity dang kết thúc
// !isDestroyed: activity đã kết thúc ,
// supportFragmentManager.isStateSaved.not() : trạng thái fragment chưa được lưu


