package com.example.myapplication.ui.main

import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.net.Uri
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.dialog.DialogHelper
import com.example.myapplication.ui.history.HistoryAudioActivity
import com.example.myapplication.ui.result.ResultActivity
import com.example.myapplication.ui.result.ResultViewModel
import com.example.myapplication.ui.selectaudio.SelectMediaActivity
import com.example.myapplication.ui.setting.SettingActivity
import com.example.myapplication.ui.video_to_audio.video_to_audio.VideoToAudioActivity
import com.example.myapplication.utils.RingtoneUtils
import com.example.myapplication.utils.showToast
import com.example.myapplication.utils.tap
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.getValue

class MainActivity : BaseActivity<ActivityMainBinding>(
    inflater = ActivityMainBinding::inflate
) {
    private val vm: MainViewModel by inject()
    private val resultViewmodel: ResultViewModel by inject()
    private val adapter by lazy { SavedAudioAdapter() }

    // MediaFile để lưu trữ tạm thời khi chọn contact
    private var selectedMediaFileForContact: MediaFile? = null

    // Activity Result Launcher for contact picker
    private val contactPickerLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK && result.data != null) {
                    result.data?.data?.let { contactUri ->
                        handleContactSelected(contactUri)
                    }
                }
            } catch (e: Exception) {
                showToast(getString(R.string.failed_to_set_contact_ringtone))
            }
        }

    override fun initView() {
        super.initView()
        setupUIHome()
        setupOptionsHome()
        setupTitleHome()
        setupRecycleview()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        vm.loadAllMedia()
    }

    fun setupUIHome() {
        binding.layoutHome1.apply {
            imgItem1.setImageResource(R.drawable.video_toaudio)
            tvItem1.text = getString(R.string.videotoaudio)

            imgItem2.setImageResource(R.drawable.audio_speed)
            tvItem2.text = getString(R.string.speed_audio)

            imgItem3.setImageResource(R.drawable.audio_text_to_audio)
            tvItem3.text = getString(R.string.random)
        }


    }

    private fun setupOptionsHome() {
        binding.tvViewHistory.tap {
            startActivity(Intent(this, HistoryAudioActivity::class.java))
        }

        binding.bottomNavHome.llSetting.tap {
            startActivity(Intent(this, SettingActivity::class.java))
        }

        binding.layoutHome1.llSpeedAudio.tap {
            val intent = SelectMediaActivity.newIntent(
                this,
                mediaType = MediaType.AUDIO,
                editType = EditType.AUDIO_SPEED
            )
            startActivity(intent)

        }

        binding.layoutHome1.llVideoToAudio.tap {
            startActivity(Intent(this, VideoToAudioActivity::class.java))
        }

    }

    private fun setupTitleHome() {
        // Gradient text
        binding.tvTitle.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height = binding.tvTitle.height.toFloat()
                if (height > 0) {
                    val textShader = LinearGradient(
                        0f, 0f, 0f, height, // gradient dọc
                        intArrayOf(
                            ContextCompat.getColor(this@MainActivity, R.color.color_FFB867_00),
                            ContextCompat.getColor(this@MainActivity, R.color.color_FF0B0B)
                        ),
                        null,
                        Shader.TileMode.CLAMP
                    )
                    binding.tvTitle.paint.shader = textShader
                    binding.tvTitle.invalidate()
                    binding.tvTitle.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })
    }

    private fun setupRecycleview() {
        binding.rvSavedAudio.adapter = adapter
        vm.allMedia.observe(this) { mediaFiles ->
            adapter.submitList(mediaFiles)
        }

        vm.loadAllMedia()

        adapter.setOnMoreClickListener { mediaFile, btnMore ->
            DialogHelper.showPopup(
                this,
                mediaFile,
                onSaveComplete = { vm.loadAllMedia() },
                onRename = { newName ->
                    lifecycleScope.launch {
                        val loadingDialog = DialogHelper.SavingDialog(this@MainActivity)
                        loadingDialog.show()
                        resultViewmodel.updateNameMediaFile(mediaFile.id, newName)
                        updateMediaFileInAdapters(mediaFile.id, newName)
                        vm.loadAllMedia()
                        loadingDialog.dismiss()
                        showToast(getString(R.string.rename_success))
                    }
                },
                onContactPicker = { selectedMediaFile ->
                    launchContactPickerForMediaFile(selectedMediaFile)
                }
            )
        }

        adapter.setOnItemClickListener { mediaFile ->
            ResultActivity.newIntent(this, mediaFile, true).also {
                startActivity(it)
            }
        }
    }

    private fun setupObservers() {
        vm.allMedia.observe(this) { mediaFiles ->
            if (mediaFiles.isNullOrEmpty()) {
                // Nếu không có dữ liệu, hiển thị layout cutter/placeholder
                binding.rvSavedAudio.visibility = View.GONE
                binding.viewEmptyItem.ss.visibility = View.VISIBLE
            } else {
                // Có dữ liệu: hiển thị RecyclerView, ẩn layout trống
                binding.rvSavedAudio.visibility = View.VISIBLE
                binding.viewEmptyItem.ss.visibility = View.GONE
                adapter.submitList(mediaFiles)
            }
        }
    }

    private fun updateMediaFileInAdapters(mediaFileId: Long, newName: String) {
        val currentList = adapter.currentList.toMutableList()
        val index = currentList.indexOfFirst { it.id == mediaFileId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(name = newName)
            adapter.submitList(null)
            adapter.submitList(currentList)
        }
    }

    private fun handleContactSelected(contactUri: Uri) {
        selectedMediaFileForContact?.let { mediaFile ->
            // Sử dụng RingtoneUtils để đặt nhạc chuông cho contact
            val success = RingtoneUtils.setContactRingtone(
                this, contactUri, mediaFile
            )
            if (!success) {
                showToast(getString(R.string.failed_to_set_contact_ringtone))
            }
            // Reset selected media file
            selectedMediaFileForContact = null
        }
    }

    // Hàm để DialogHelper có thể gọi khi cần mở contact picker
    fun launchContactPickerForMediaFile(mediaFile: MediaFile) {
        selectedMediaFileForContact = mediaFile
        try {
            val intent = RingtoneUtils.getContactPickerIntent()
            contactPickerLauncher.launch(intent)
        } catch (e: Exception) {
            showToast(getString(R.string.cannot_open_contacts))
        }
    }
}