package com.example.myapplication.ui.video_to_audio.video_to_audio

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityVideoToAudioBinding
import com.example.myapplication.ui.video_to_audio.convert.ConvertActivity
import com.example.myapplication.utils.const.EXTRA_SELECTED_URIS
import com.example.myapplication.utils.showToast
import com.example.myapplication.utils.tap
import kotlin.getValue
import kotlin.toString

class VideoToAudioActivity : BaseActivity<ActivityVideoToAudioBinding>(
    inflater = ActivityVideoToAudioBinding::inflate
) {
    private val viewModel: VideoToAudioViewModel by viewModels()
    private lateinit var adapter: VideoAdapter
    private lateinit var selectedAdapter: SelectedVideoAdapter

    private val convertLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedUris =
                    result.data?.getStringArrayExtra(EXTRA_SELECTED_URIS)?.toList() ?: emptyList()

                if (selectedUris.isEmpty()) {
                    // Clear all selections
                    adapter.clearSelection()
                } else {
                    // Update adapter selection to match the returned URIs
                    val allVideos = adapter.currentList
                    val updatedSelection = selectedUris.mapNotNull { uriStr ->
                        allVideos.find { it.uri.toString() == uriStr }
                    }
                    adapter.updateSelection(updatedSelection)
                }
            }
        }

    override fun initView() {
        super.initView()
        setupRecyclerView()
        setupClickListeners()
        openVideoPicker()
        initToolBar()

        viewModel.allVideos.observe(this) { allVideos ->
            adapter.submitList(allVideos)
        }

        binding.toolBar.tvTitle.tap {
            openVideoPicker()
        }
    }

    private fun initToolBar() {
        binding.toolBar.apply {
            ivRightIcon2.visibility = View.GONE
            ivRightIcon1.visibility = View.GONE // Initially hidden until videos are selected
            ivRightIcon1.setImageResource(R.drawable.btn_next)
            tvTitle.text = getString(R.string.select_audio)
        }
    }

    private fun setupRecyclerView() {
        // Main video grid adapter
        adapter = VideoAdapter().apply {
            setOnMultiSelectionChanged { selectedVideos ->
                // Update UI based on selection
                if (selectedVideos.isNotEmpty()) {
                    binding.bottomSelectionBar.visibility = View.VISIBLE
                    binding.selectedCountText.text = getString(
                        R.string.of_selected,
                        selectedVideos.size
                    )
                    binding.toolBar.ivRightIcon1.visibility = View.VISIBLE
                } else {
                    binding.bottomSelectionBar.visibility = View.GONE
                    binding.toolBar.ivRightIcon1.visibility = View.GONE
                }

                // Update selected videos recycler
                selectedAdapter.submitList(selectedVideos)
            }

            setOnLimitReached {
                this@VideoToAudioActivity.showToast(getString(R.string.select_max_4))
            }
            setOnLessThanLimitReached {
                this@VideoToAudioActivity.showToast(getString(R.string.please_select_a_media_file_at_least_5_seconds_long))
            }
        }

        // Selected videos adapter
        selectedAdapter = SelectedVideoAdapter()

        selectedAdapter.setOnRemoveClick { videoItem ->
            adapter.toggleSelection(videoItem)
        }

        binding.recyclerViewVideos.adapter = adapter
        binding.selectedVideosRecycler.adapter = selectedAdapter
    }

    private fun setupClickListeners() {
        binding.toolBar.ivLeftIcon.tap { finish() }

        binding.toolBar.ivRightIcon1.tap {
            val selectedVideos = adapter.getSelectedItems()
            if (selectedVideos.isNotEmpty()) {
                val uris = selectedVideos.map { it.uri.toString() }.toTypedArray()
                val intent = ConvertActivity.newIntent(this, uris)
                convertLauncher.launch(intent)
            } else {
                Toast.makeText(this, getString(R.string.please_select_video), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val pickVideosLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickMultipleVisualMedia(4)
        ) { uris ->
            if (uris.isNotEmpty()) {
                viewModel.setPickedVideos(uris)
            }
        }

    private fun openVideoPicker() {
        pickVideosLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }



}