package com.example.myapplication.ui.video_to_audio.convert

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.databinding.ActivityConvertBinding
import com.example.myapplication.ui.result.SavingProgressActivity
import com.example.myapplication.ui.video_to_audio.video_to_audio.VideoItem
import com.example.myapplication.utils.const.EXTRA_SELECTED_URIS
import com.example.myapplication.utils.generateOutputFileName
import com.example.myapplication.utils.tap
import kotlin.apply
import kotlin.collections.map
import kotlin.compareTo
import kotlin.getValue

class ConvertActivity :
    BaseActivity<ActivityConvertBinding>(inflater = ActivityConvertBinding::inflate) {

    private val viewModel: ConvertVTAViewModel by viewModels()
    private lateinit var adapter: ConvertAdapter

    companion object {
        fun newIntent(context: Context, selectedVideoUris: Array<String>): Intent =
            Intent(context, ConvertActivity::class.java).apply {
                putExtra(EXTRA_SELECTED_URIS, selectedVideoUris)
            }
    }

    override fun initView() {
        super.initView()
        setupAdapter()
        setupFormatButtons()
        setupClickListeners()
        observeViewModel()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // xử lý thay cho onBackPressed()
                    finishWithResult()
                }
            })

        // get initial selection passed from VideoToAudioActivity
        val uris = intent.getStringArrayExtra(EXTRA_SELECTED_URIS) ?: arrayOf()
        val items = uris.map { uriStr -> getVideoItemFromUri(uriStr.toUri()) }
        viewModel.setVideos(items)
    }

    private fun getVideoItemFromUri(uri: Uri): VideoItem {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val name = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME).let { idx ->
                    if (idx >= 0) cursor.getString(idx) else getString(R.string.unknown)
                }
                val size = cursor.getColumnIndex(MediaStore.Video.Media.SIZE).let { idx ->
                    if (idx >= 0) cursor.getLong(idx) else 0L
                }
                val duration = cursor.getColumnIndex(MediaStore.Video.Media.DURATION).let { idx ->
                    if (idx >= 0) cursor.getLong(idx) else 0L
                }
                return VideoItem (
                    uri,
                    name ?: getString(R.string.unknown),
                    duration,
                    size,
                    uri.toString()
                )
            }
        }
        return VideoItem(uri, getString(R.string.unknown), 0L, 0L, uri.toString())
    }

    private fun setupAdapter() {
        adapter = ConvertAdapter().apply{
            onDeleteClick = { video ->
                viewModel.removeVideo(video)
            }
        }

        binding.recyclerViewConvertFiles.apply {
            layoutManager = LinearLayoutManager(this@ConvertActivity)
            setHasFixedSize(true)
            adapter = this@ConvertActivity.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.selectedVideos.observe(this) { list ->
            if (list.isEmpty()) finishWithResult()
            adapter.submitList(list.toList())
            updateAddFileVisibility(list.size)
        }

        viewModel.selectedFormat.observe(this) { format ->
            highlightFormat(format)
        }

    }

    private fun setupFormatButtons() {
        binding.layoutFormat.btnConvert.text = getString(R.string.convert)
        binding.layoutFormat.btnMp3.tap { viewModel.selectFormat(FormatType.MP3) }
        binding.layoutFormat.btnWav.tap { viewModel.selectFormat(FormatType.WAV) }
        binding.layoutFormat.btnAac.tap { viewModel.selectFormat(FormatType.AAC) }
        binding.layoutFormat.btnFlac.tap { viewModel.selectFormat(FormatType.FLAC) }
        binding.layoutFormat.btnOgg.tap { viewModel.selectFormat(FormatType.OGG) }
    }

    private fun highlightFormat(format: FormatType) {
        val buttons = mapOf(
            FormatType.MP3 to binding.layoutFormat.btnMp3,
            FormatType.WAV to binding.layoutFormat.btnWav,
            FormatType.AAC to binding.layoutFormat.btnAac,
            FormatType.FLAC to binding.layoutFormat.btnFlac,
            FormatType.OGG to binding.layoutFormat.btnOgg
        )

        buttons.values.forEach { btn ->
            btn.setTextAppearance(R.style.FormatButton)
            btn.setBackgroundResource(R.drawable.bg_round_8_border)
        }

        buttons[format]?.apply {
            setTextAppearance(R.style.FormatButtonSelected)
            setBackgroundResource(R.drawable.bg_radius_8_ee5300)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.ivLeftIcon.tap {
            // return updated list to caller
            finishWithResult()
        }
        binding.toolbar.tvTitle.text = getString(R.string.convert)
        binding.toolbar.ivRightIcon2.isVisible = false

        // Add click event for tvAddFile that finishes with result
        binding.toolbar.tvAddFile.tap {
            finishWithResult()
        }

        binding.layoutFormat.btnConvert.tap {
            startConversion()
        }
    }

    private fun startConversion() {
        val selectedVideos = viewModel.selectedVideos.value ?: return
        val selectedFormat = viewModel.selectedFormat.value ?: FormatType.MP3

        // Convert videos to audio MediaFiles
        val audioFiles = selectedVideos.map { video ->
            // Clean up file name and remove extension
            val outputFileName =
                generateOutputFileName("videoToAudio", FormatType.MP3).substringBeforeLast(".")

            MediaFile(
                id = 0, // Temporary ID, will be updated after saving
                name = outputFileName, // Add new extension
                duration = video.duration, // Keep original duration
                size = video.size, // Keep original size for now
                dateAdded = System.currentTimeMillis(),
                uri = video.uri, // Temporary URI, will be updated after conversion
                format = FormatType.valueOf(selectedFormat.name), // New audio format
                editType = EditType.VIDEO_TO_AUDIO,
                mediaType = MediaType.AUDIO,
                selectionOrder = 0
            )
        }

        // Start SavingProgressActivity with both video and audio information
        val intent = SavingProgressActivity.newConvertIntent(
            context = this,
            videos = selectedVideos,
            audio = audioFiles,
            format = FormatType.valueOf(selectedFormat.name)
        )
        startActivity(intent)
        finish()
    }

    private fun updateAddFileVisibility(currentCount: Int) {
        // Show tvAddFile when there are fewer than 4 videos selected
        binding.toolbar.tvAddFile.visibility = if (currentCount < 4) View.VISIBLE else View.GONE
    }

    private fun finishWithResult() {
        val list = viewModel.selectedVideos.value ?: emptyList()
        val intent = Intent().apply {
            putExtra(EXTRA_SELECTED_URIS, list.map { it.uri.toString() }.toTypedArray())
        }
        setResult(RESULT_OK, intent)
        finish()
    }
}