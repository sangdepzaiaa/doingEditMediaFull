package com.example.myapplication.ui.video_to_audio.result

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.ActivityResultVideoToAudioBinding
import com.example.myapplication.ui.result.ResultActivity
import com.example.myapplication.ui.result.ResultViewModel
import com.example.myapplication.utils.const
import com.example.myapplication.utils.const.EXTRA_MEDIA_FILES
import com.example.myapplication.utils.getParcelableArrayListCompat
import com.example.myapplication.utils.getParcelableExtraCompat
import com.example.myapplication.utils.tap
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class ResultVideoToAudioActivity :
    BaseActivity<ActivityResultVideoToAudioBinding>(ActivityResultVideoToAudioBinding::inflate) {

    private val viewModel: ResultViewModel by inject()
    private var adapter: ResultAdapter? = null

    // Register for activity result to handle callback from ResultActivity
    private val resultActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val isDelete = result.data?.getBooleanExtra(const.ACTION_DELETE, false) ?: false
            val mediaFile = result.data?.getParcelableExtraCompat(
                const.EXTRA_MEDIA_FILES, // Fixed: Use the same key as in ResultActivity
                MediaFile::class.java
            )

            mediaFile?.let { file ->
                if (isDelete) {
                    // Remove the deleted item from adapter
                    adapter?.deleteItem(file)
                } else {
                    // Update the renamed item in adapter
                    adapter?.updateItem(file)
                }
            }
            Log.d("TAG", ":$mediaFile ")
        }
    }

    companion object {
        fun newIntent(context: Context, mediaFiles: List<MediaFile>): Intent =
            Intent(context, ResultVideoToAudioActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_FILES, ArrayList(mediaFiles))
            }
    }

    override fun initView() {
        super.initView()
        binding.toolbar.ivLeftIcon.tap { finish() }
        binding.toolbar.ivRightIcon2.isVisible = false
        binding.toolbar.tvTitle.text = getString(R.string.result)

        adapter = ResultAdapter().apply {
            setOnItemClickListener { mediaFile ->
                if (mediaFile.id != 0L) {
                    val intent =
                        ResultActivity.newIntent(this@ResultVideoToAudioActivity, mediaFile)
                    resultActivityLauncher.launch(intent)
                } else {
                    Toast.makeText(
                        this@ResultVideoToAudioActivity,
                        getString(R.string.file_not_ready),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        loadMediaFiles()
    }

    private fun loadMediaFiles() {
        val mediaFiles =
            intent.getParcelableArrayListCompat<MediaFile>(const.EXTRA_MEDIA_FILES)?.toList()
                ?: emptyList()

        if (mediaFiles.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_files), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                adapter?.updateData(mediaFiles)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@ResultVideoToAudioActivity,
                    getString(R.string.error),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
