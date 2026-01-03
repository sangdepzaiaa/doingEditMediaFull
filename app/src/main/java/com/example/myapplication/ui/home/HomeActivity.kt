package com.example.myapplication.ui.home


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.FaceDetectionResult
import com.example.myapplication.data.remote.RetrofitClient
import com.example.myapplication.databinding.ActivityHomeBinding
import com.example.myapplication.ui.dialog.DialogCheckFaceId
import com.example.myapplication.ui.dialog.DialogTypeChoosePhoto
import com.example.myapplication.ui.permission.PermissionActivity
import com.example.myapplication.utils.copyToCacheFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream

class HomeActivity : BaseActivity<ActivityHomeBinding>(
    inflater = ActivityHomeBinding::inflate
) {
    var tempFile: File? = null
    var currentPhoto: File? = null
    var currentPhoto2: File? = null
    private lateinit var adapter: ImageAdapter
    private var currentImageId: String? = null

    private var selectedImageUri: Uri? = null

    val pickContent = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { handleUri(it) }
    }

    val pickVisual = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { handleUri(it) }
    }

    val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempFile?.let { checkFace(it) }
        } else {
            tempFile?.delete()
            tempFile = null
        }
    }

    var isChooseTypePhoto = true
    var photoBottomSheet: DialogTypeChoosePhoto? = null
    fun showChooseTypePhoto() {
        photoBottomSheet?.dismissAllowingStateLoss()
        photoBottomSheet = null

        photoBottomSheet = DialogTypeChoosePhoto(object : DialogTypeChoosePhoto.OnSelectedListener {
            override fun onPhotoSelected() {
                photoBottomSheet = null
                openPickPhoto()
            }

            override fun onCameraSelected() {
                photoBottomSheet = null
                openCamera()
            }

        })

        if (!isDestroyed && !isFinishing && supportFragmentManager.isStateSaved.not()) {
            photoBottomSheet?.show(supportFragmentManager, "ChoosePhotoDialog")
        }
    }

    fun openPickPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            pickVisual.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            pickContent.launch("image/*")
        }
    }

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(this@HomeActivity, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun openCamera() {
        if (!hasPermission()) {
            startActivity(Intent(this, PermissionActivity::class.java))
            return
        }
        openCameraInternal()
    }

    fun openCameraInternal() {
        tempFile = File(cacheDir, "camera_${System.currentTimeMillis()}.jpg")
        val uri =
            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}", tempFile ?: return)
        takePhoto
    }

    fun handleUri(uri: Uri) {
        var file =
            uri.copyToCacheFile(this@HomeActivity, "picked_photo_${System.currentTimeMillis()}.jpg")
        file?.let { checkFace(it) }
    }

    fun checkFace(file: File) {
        // file không thể null ở đây (do đã kiểm tra trước khi gọi), nhưng vẫn giữ an toàn
        if (!file.exists()) {
            Toast.makeText(this, R.string.error, Toast.LENGTH_SHORT).show()
            return
        }

//        // Hiển thị ảnh lên ImageView (chỉ gọi 1 lần duy nhất)
//        binding.generateAiArts.imgPhoto.setImageURI(Uri.fromFile(file))

        // Kiểm tra khuôn mặt bằng ML Kit
        detectFaceInImage(file) { result ->
            lifecycleScope.launch {
                when (result) {
                    // 1. OK → khuôn mặt chính diện, rõ ràng → đi tiếp
                    FaceDetectionResult.SingleGoodFace -> {
                        //updateImageAndFile(file)
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

//    fun  updateImageAndFile(file: File){
//        val targetImageView = if (isChooseTypePhoto) binding.imgPhoto else binding.imgYourStyle
//
//        Glide.with(this)
//            .load(file)
//            .centerCrop()
//            .placeholder(R.drawable.img_banner)
//            .into(targetImageView)
//
//        if (isChooseTypePhoto) {
//            currentPhoto = file
//        } else {
//            currentPhoto2 = file
//        }
//
//        Toast.makeText(this, "Ảnh đã được chọn thành công!", Toast.LENGTH_SHORT).show()
//    }

    private fun detectFaceInImage(file: File, onResult: (FaceDetectionResult) -> Unit) {
        val image = InputImage.fromFilePath(this, Uri.fromFile(file))

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .setMinFaceSize(0.05f)
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
                        val isFrontal =
                            kotlin.math.abs(headEulerY) < 35 && kotlin.math.abs(headEulerZ) < 35
                        val isEyesOpen = (leftEyeOpen > 0.5f && rightEyeOpen > 0.5f)

                        if (isFrontal) {
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
        currentPhoto?.delete()
        currentPhoto2?.delete()
        super.onDestroy()
    }

    override fun initView() {
        super.initView()
        setupRecyclerView()
        loadDataFromServer() // Bước 1: GET dữ liệu lên RecyclerView

        // Sự kiện chọn ảnh từ thư viện
        binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        // Sự kiện POST dữ liệu lên server
        binding.btnUpload.setOnClickListener {
            uploadDataToServer()
        }
    }

    private fun setupRecyclerView() {
        adapter = ImageAdapter(emptyList()) { item ->
            Toast.makeText(this, "Bạn chọn: ${item.title}", Toast.LENGTH_SHORT).show()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    // --- PHẦN 1: GET DỮ LIỆU ---
    private fun loadDataFromServer() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.getListItems()
                adapter.updateData(response)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@HomeActivity, "Lỗi GET: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- PHẦN 2: POST DỮ LIỆU ---
    private fun uploadDataToServer() {
        val uri = selectedImageUri ?: return
        if (uri == null) {
            Toast.makeText(this, "Vui lòng chọn ảnh trước!", Toast.LENGTH_SHORT).show()
            return
        }

        // Lấy text từ EditText (Giả sử bạn có 2 ô nhập liệu này)
        val title = binding.edtTitle.text.toString()
        val desc = binding.edtDescription.text.toString()

        lifecycleScope.launch {
            try {
                // Chuyển Uri thành File để gửi
                val file = uriToFile(uri)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("file", file.name, requestFile)

                val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
                val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())

                // Gọi API POST
                val responseBody = RetrofitClient.instance.
                uploadData(titlePart, descPart, imagePart)

                // Nhận lại ảnh đã chỉnh sửa từ server và hiển thị lên ImageView
                val bitmap = BitmapFactory.decodeStream(responseBody.byteStream())
                binding.imgResult.setImageBitmap(bitmap)

                Toast.makeText(this@HomeActivity, "Upload & Nhận ảnh thành công!", Toast.LENGTH_SHORT).show()

                // Load lại danh sách RecyclerView để cập nhật ảnh mới
                loadDataFromServer()

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@HomeActivity, "Lỗi POST: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher để lấy kết quả chọn ảnh
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.imgPreview.setImageURI(selectedImageUri) // Hiển thị ảnh vừa chọn để xem trước
        }
    }

    // Hàm phụ trợ chuyển Uri sang File (bắt buộc để gửi Multipart)
    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "temp_image.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        return file
    }
}





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


