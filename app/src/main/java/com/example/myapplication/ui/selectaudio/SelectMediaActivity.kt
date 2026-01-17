package com.example.myapplication.ui.selectaudio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.enumm.MediaType
import com.example.myapplication.data.enumm.SelectionMode
import com.example.myapplication.databinding.ActivitySelectMediaBinding
import com.example.myapplication.utils.const.EDIT_TYPE
import com.example.myapplication.utils.const.EXTRA_MEDIA_TYPE
import com.example.myapplication.utils.getSerializableCompat
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class SelectMediaActivity : BaseActivity<ActivitySelectMediaBinding>(
    inflater = ActivitySelectMediaBinding::inflate
) {

    private lateinit var mediaType: MediaType
    private lateinit var editType: EditType
    private lateinit var selectionMode: SelectionMode
    private val viewModel: MediaViewModel by viewModel {
        parametersOf(mediaType)
    }
    private lateinit var filesAdapter: MediaFileAdapter
    private lateinit var foldersAdapter: MediaFolderAdapter
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.entries.all { it.value }) {
                loadInitialData()
            } else {
                Toast.makeText(this, getString(R.string.permission_is_required), Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }

    private val convertLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedFiles =
                    result.data?.getSerializableCompat<ArrayList<MediaFile>>(UPDATED_MEDIA_FILES)
                        ?: arrayListOf()
                filesAdapter.updateSelection(updatedFiles)
            }
        }

    private val mixerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedFiles =
                    result.data?.getSerializableCompat<ArrayList<MediaFile>>(UPDATED_MEDIA_FILES)
                        ?: arrayListOf()
                filesAdapter.updateSelection(updatedFiles)
            }
        }

    private val mergerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val updatedFiles =
                    result.data?.getSerializableCompat<ArrayList<MediaFile>>(UPDATED_MEDIA_FILES)
                        ?: arrayListOf()
                filesAdapter.updateSelection(updatedFiles)
            }
        }

    override fun initView() {
        editType = intent.getSerializableCompat(EDIT_TYPE) as? EditType ?: EditType.AUDIO_CUTTER
        selectionMode = when (editType) {
            EditType.AUDIO_MERGER, EditType.AUDIO_MIXER, EditType.AUDIO_CONVERTER -> SelectionMode.MULTIPLE
            else -> SelectionMode.SINGLE
        }
        mediaType = intent.getSerializableCompat(EXTRA_MEDIA_TYPE) as? MediaType ?: MediaType.AUDIO
        viewBinding.toolbar.tvTitle.text = getString(R.string.select_audio)
        viewBinding.toolbar.ivRightIcon1.visibility = View.GONE
        viewBinding.toolbar.ivRightIcon2.visibility = View.GONE
        setupAdapters()
        setupTabs()
        setupSorting()
        setupOnClickListener()
        handleCustomBackButton()
    }

    override fun bindView() {
        checkAndRequestPermissions()
    }

    override fun bindViewModel() {
        viewModel.uiState.observe(this) { state ->
            if (selectionMode == SelectionMode.MULTIPLE) {
                filesAdapter.clearSelection()
            }

            viewBinding.llSortContainer.visibility = when (state) {
                is MediaScreenState.AllFiles, is MediaScreenState.FilesInFolder -> View.VISIBLE
                is MediaScreenState.Folders -> View.GONE
            }
            when (state) {
                is MediaScreenState.AllFiles -> {
                    viewBinding.rvFolders.visibility = View.GONE
                    viewBinding.rvFiles.visibility = View.VISIBLE
                    filesAdapter.submitList(viewModel.allFiles.value ?: emptyList())
                    viewBinding.tabLayout.getTabAt(0)?.select()
                    updateEmptyViewVisibility(viewModel.allFiles.value?.isEmpty() == true)
                }

                is MediaScreenState.Folders -> {
                    viewBinding.rvFiles.visibility = View.GONE
                    viewBinding.rvFolders.visibility = View.VISIBLE
                    viewBinding.tabLayout.getTabAt(1)?.select()
                }

                is MediaScreenState.FilesInFolder -> {
                    viewBinding.rvFolders.visibility = View.GONE
                    viewBinding.rvFiles.visibility = View.VISIBLE
                    filesAdapter.submitList(emptyList())
                }
            }
        }

        viewModel.sortCriteria.observe(this) { criteria ->
            val criteriaText = when (criteria) {
                SortCriteria.NAME -> getString(R.string.name)
                SortCriteria.DURATION -> getString(R.string.duration)
                SortCriteria.DATE -> getString(R.string.date)
            }
            viewBinding.tvSortCriteria.text = getString(R.string.sorted_by, criteriaText)
        }

        viewModel.sortOrder.observe(this) { order ->
            val rotationAngle = if (order == SortOrder.ASCENDING) 180f else 0f
            viewBinding.ivSortDirection.animate()
                .rotation(rotationAngle)
                .setDuration(300)
                .start()
        }

        viewModel.allFiles.observe(this) { files ->
            if (viewModel.uiState.value is MediaScreenState.AllFiles) {
                filesAdapter.submitList(files)
            }
        }
        viewModel.folders.observe(this) { folders ->
            foldersAdapter.submitList(folders)
        }
        viewModel.filesInFolder.observe(this) { filesInFolder ->
            if (viewModel.uiState.value is MediaScreenState.FilesInFolder) {
                filesAdapter.submitList(ArrayList(filesInFolder))
            }
        }
    }

    private fun calculateTotalDuration(files: List<MediaFile>): Long {
        return files.sumOf { it.duration }
    }

    private fun setupSorting() {
        viewBinding.llSortContainer.tap {
            SortDialog(this, viewModel).show()
        }
    }

    private fun setupOnClickListener() {
        viewBinding.btnNext.tap {
            val selectedFiles: List<MediaFile> = filesAdapter.getSelectedItems()
            if (selectedFiles.isNotEmpty()) {
                val totalDuration = calculateTotalDuration(selectedFiles)

                val hasShortFile = selectedFiles.any { it.duration < MIN_SELECTED_MEDIA_DURATION_MS }
                if (hasShortFile) {
                    showToast(getString(R.string.please_select_a_media_file_at_least_5_seconds_long))
                    return@tap
                }

                if (totalDuration > MAX_TOTAL_SELECTION_DURATION_MS && editType == EditType.AUDIO_MERGER) {
                    showToast(getString(R.string.please_select_audio_files_with_a_total_duration_of_up_to_30_minutes))
                    return@tap
                }

                when (editType) {
                    EditType.AUDIO_MERGER -> {
                        val intent = MergerActivity.newIntent(this, selectedFiles)
                        mergerLauncher.launch(intent)
                    }

                    EditType.AUDIO_MIXER -> {
                        val intent =
                            MixerActivity.newIntent(this, selectedFiles, EditType.AUDIO_MIXER)
                        mixerLauncher.launch(intent)
                    }

                    EditType.AUDIO_CONVERTER -> {
                        val intent = ConfigConvertActivity.newIntent(this, selectedFiles)
                        convertLauncher.launch(intent)
                    }

                    else -> {
                        // No action needed for single selection modes here
                    }
                }
            } else {
                showToast("No files selected.")
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupAdapters() {
        filesAdapter = MediaFileAdapter()
        filesAdapter.setSelectionMode(selectionMode)
        filesAdapter.setEditType(editType)
        viewBinding.rvFiles.adapter = filesAdapter
        viewBinding.rvFiles.layoutManager = LinearLayoutManager(this)

        if (selectionMode == SelectionMode.SINGLE) {
            filesAdapter.setOnSingleItemSelected { mediaFile ->
                if (mediaFile.duration < MIN_SELECTED_MEDIA_DURATION_MS) {
                    showToast(getString(R.string.please_select_a_media_file_at_least_5_seconds_long))
                    return@setOnSingleItemSelected
                }
                if (mediaFile.duration > MAX_TOTAL_SELECTION_DURATION_MS) {
                    showToast(getString(R.string.please_select_audio_files_with_a_total_duration_of_up_to_30_minutes))
                    return@setOnSingleItemSelected
                }
                onMediaItemClick(editType, mediaFile)
            }
        } else {
            filesAdapter.setOnMultiSelectionChanged { selectedFiles ->
                if (selectedFiles.isNotEmpty()) { // Your requirement for the bottom bar
                    viewBinding.selectionContainer.visibility = View.VISIBLE
                    viewBinding.tvSelectionCount.text =
                        resources.getQuantityString(
                            R.plurals.files_selected,
                            selectedFiles.size,
                            selectedFiles.size
                        )
                } else {
                    viewBinding.selectionContainer.visibility = View.GONE
                }
            }
            filesAdapter.setOnLimitReached {
                showToast("Max file selected")
            }

            filesAdapter.setOnLessThanLimitReached{
                showToast(getString(R.string.please_select_a_media_file_at_least_5_seconds_long))
            }
        }

        foldersAdapter = MediaFolderAdapter { folder ->
            filesAdapter.setItems(emptyList())
            filesAdapter.notifyDataSetChanged()
            viewModel.onFolderClicked(folder)
        }

        viewBinding.rvFolders.apply {
            adapter = foldersAdapter
            layoutManager = LinearLayoutManager(this@SelectMediaActivity)
        }
    }

    private fun onMediaItemClick(editType: EditType, mediaFile: MediaFile) {
        when (editType) {
            EditType.NOTHING -> {}
            EditType.AUDIO_CUTTER -> {
                val intent = CutAudioActivity.newIntent(this, mediaFile, EditType.AUDIO_CUTTER)
                startActivity(intent)
            }

            EditType.VIDEO_TO_AUDIO -> {}
            EditType.VOICE_CHANGE -> {}
            EditType.TEXT_TO_AUDIO -> {}
            EditType.AUDIO_VOLUME -> {
                val intent = AudioBoosterActivity.newIntent(this, mediaFile)
                convertLauncher.launch(intent)
            }

            EditType.AUDIO_MERGER -> {}
            EditType.AUDIO_MIXER -> {}

            EditType.AUDIO_CONVERTER -> {}
            EditType.AUDIO_SPEED -> {
                val intent = EditSpeedAudioActivity.newIntent(this, mediaFile)
                startActivity(intent)
            }

            EditType.AUDIO_EFFECT_CHANGER -> {
                val bundleModel = BundleModel(
                    soundPath = Utils.getRealPathFromURI(
                        this@SelectMediaActivity,
                        mediaFile.uri, mediaFile.name, mediaFile.format
                    ) ?: ""
                )
                val intent = Intent(this, VoiceChangeActivity::class.java)
                intent.putExtra(Constants.BUNDLE_PATH_SOUND, bundleModel)
                intent.putExtra(Constants.EXTRA_MEDIA_FILE, mediaFile)
                startActivity(intent)
            }
        }
    }

    private fun setupTabs() {
        viewBinding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { viewModel.onTabSelected(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (mediaType) {
                MediaType.AUDIO -> arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
                MediaType.VIDEO -> arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            }
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.all {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) == PackageManager.PERMISSION_GRANTED
            }) {
            loadInitialData()
        } else {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }

    private fun handleCustomBackButton() {
        viewBinding.toolbar.ivLeftIcon.tap {
            if (viewModel.uiState.value is MediaScreenState.FilesInFolder) {
                viewModel.onBackPressed()
            } else {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun loadInitialData() {
        viewModel.loadInitialData()
    }

    private fun updateEmptyViewVisibility(isEmpty: Boolean) {
        viewBinding.viewEmptyItem.root.visibility = if (isEmpty) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    companion object {
        fun newIntent(context: Context, mediaType: MediaType, editType: EditType): Intent {
            return Intent(context, SelectMediaActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_TYPE, mediaType)
                putExtra(EDIT_TYPE, editType)
            }
        }
    }
}