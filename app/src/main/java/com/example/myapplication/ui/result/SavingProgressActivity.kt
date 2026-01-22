package com.example.myapplication.ui.result

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.Session
import com.arthenica.ffmpegkit.ReturnCode
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.data.repository.MediaFileRepository
import com.example.myapplication.databinding.ActivitySavingProgressBinding
import com.example.myapplication.ui.dialog.DialogHelper
import com.example.myapplication.ui.history.MediaFileAdapter
import com.example.myapplication.ui.speed_audio.FileUtils
import com.example.myapplication.ui.video_to_audio.result.ResultVideoToAudioActivity
import com.example.myapplication.ui.video_to_audio.video_to_audio.VideoItem
import com.example.myapplication.utils.MediaScanner
import com.example.myapplication.utils.const.EDIT_TYPE
import com.example.myapplication.utils.const.EXTRA_FORMAT
import com.example.myapplication.utils.const.EXTRA_RESULTS
import com.example.myapplication.utils.const.EXTRA_SPEED
import com.example.myapplication.utils.const.EXTRA_VIDEOS
import com.example.myapplication.utils.generateOutputFile
import com.example.myapplication.utils.generateOutputFileName
import com.example.myapplication.utils.getParcelableArrayListCompat
import com.example.myapplication.utils.getSerializableCompat
import com.example.myapplication.utils.gone
import com.example.myapplication.utils.tap
import com.example.myapplication.utils.toMediaEntry
import com.example.myapplication.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import kotlin.getValue

@SuppressLint("SetTextI18n")
class SavingProgressActivity :
    BaseActivity<ActivitySavingProgressBinding>(ActivitySavingProgressBinding::inflate) {
    companion object {

        fun newConvertIntent(
            context: Context,
            videos: List<VideoItem>,
            audio: List<MediaFile>,
            format: FormatType
        ): Intent {
            return Intent(context, SavingProgressActivity::class.java).apply {
                putParcelableArrayListExtra(EXTRA_VIDEOS, ArrayList(videos))
                putParcelableArrayListExtra(EXTRA_RESULTS, ArrayList(audio))
                putExtra(EXTRA_FORMAT, format.name)
            }
        }
        fun newSpeedIntent(
            context: Context,
            speed: Float
        ): Intent {
            return Intent(context, SavingProgressActivity::class.java).apply {
                putExtra(EDIT_TYPE, EditType.AUDIO_SPEED)
                putExtra(EXTRA_SPEED, speed)
            }
        }
    }

    private var filesAdapter: MediaFileAdapter? = null
    private val mediaFileRepository: MediaFileRepository by inject()

    override fun bindView() {
        super.bindView()
        binding.toolbar.tvTitle.text = getString(R.string.convertion)
        binding.toolbar.ivLeftIcon.tap {
            DialogHelper.showExitDialogDialog(
                this@SavingProgressActivity,
                getString(R.string.alert),
                getString(R.string.do_you_want_to_cancel_the_conversion),
                getString(R.string.cancel),
                getString(R.string.ok)
            ) { finish() }
        }
    }

    override fun onResume() {
        super.onResume()

        val results = intent.getParcelableArrayListCompat<MediaFile>(EXTRA_RESULTS) ?: emptyList()
        showResultFiles(results)

        val editType = intent.getSerializableCompat<EditType>(EDIT_TYPE)

        val isVideoConversion = intent.hasExtra(EXTRA_VIDEOS) && intent.hasExtra(EXTRA_FORMAT)
        val isSpeedChange = editType == EditType.AUDIO_SPEED

        when {
            isVideoConversion -> handleVideoConversion()
            isSpeedChange -> handleSpeedConversion()
        }
    }

    private fun handleVideoConversion() {
        val videos = intent.getParcelableArrayListCompat<VideoItem>(EXTRA_VIDEOS)
        if (videos == null || videos.isEmpty()) return

        val formatName = intent.getStringExtra(EXTRA_FORMAT)
        if (videos.isNotEmpty() && formatName != null) {
            val format = FormatType.valueOf(formatName)
            lifecycleScope.launch {
                val results = mutableListOf<MediaFile>()
                for (rawVideo in videos) {
                    val video =
                        if (rawVideo.name.isBlank() || rawVideo.name == "Unknown") rawVideo else rawVideo
                    val outputFileName = generateOutputFileName(
                        "videoToAudio",
                        FormatType.MP3
                    ).substringBeforeLast(".")
                    val tempFile = File.createTempFile(
                        "temp_convert_${System.currentTimeMillis()}_",
                        ".${format.name.lowercase()}",
                        cacheDir
                    )
                    if (tempFile.exists()) tempFile.delete()
                    val inputPath = getPathFromUri(video.uri) ?: continue
                    val outputPath = tempFile.absolutePath

                    val command = when (format) {
                        FormatType.MP3 -> "-y -i \"$inputPath\" -vn -c:a libmp3lame -q:a 2 \"$outputPath\""
                        FormatType.WAV -> "-y -i \"$inputPath\" -vn -c:a pcm_s16le \"$outputPath\""
                        FormatType.AAC -> "-y -i \"$inputPath\" -vn -c:a aac -b:a 192k \"$outputPath\""
                        FormatType.FLAC -> "-y -i \"$inputPath\" -vn -c:a flac \"$outputPath\""
                        FormatType.OGG -> "-y -i \"$inputPath\" -vn -c:a libvorbis \"$outputPath\""
                    }

                    var lastProgress = 0
                    val session = executeFfmpegAsyncBlocking(command) { statistics ->
                        val totalDuration = video.duration
                        val time = statistics.time
                        val progress = if (totalDuration > 0) {
                            (time * 100 / totalDuration).toInt().coerceIn(0, 100)
                        } else 0
                        if (progress != lastProgress) {
                            lastProgress = progress
                            runOnUiThread {
                                binding.seekBarProgress.progress = progress
                                binding.tvProgressPercent.text = "${progress}%"
                            }
                        }
                    }

                    if (ReturnCode.isSuccess(session.returnCode)) {
                        val scanResult = MediaScanner.scanFile(
                            this@SavingProgressActivity,
                            tempFile,
                            EditType.VIDEO_TO_AUDIO,
                            outputFileName,
                            FormatType.valueOf(format.name)
                        )
                        if (scanResult != null) {
                            val (newMediaFile, _) = scanResult
                            val mediaEntityToInsert = newMediaFile.toMediaEntry()
                            val id = mediaFileRepository.addMedia(mediaEntityToInsert)
                            results.add(newMediaFile.copy(id))
                        }
                    }
                }

                // After all conversions
                if (results.isNotEmpty()) {
                    startActivity(
                        ResultVideoToAudioActivity.newIntent(
                            this@SavingProgressActivity,
                            results
                        )
                    )
                    finish()
                } else {
                    Toast.makeText(
                        this@SavingProgressActivity,
                        "Conversion failed",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    @SuppressLint("Range")
    private fun getPathFromUri(uri: Uri): String? {

        if ("file".equals(uri.scheme, true)) return uri.path
        if ("content".equals(uri.scheme, true)) {
            return try {
                val projection = arrayOf(MediaStore.MediaColumns.DATA)
                contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                        cursor.getString(columnIndex)
                    } else null
                }
            } catch (_: Exception) {
                try {
                    val inputStream = contentResolver.openInputStream(uri) ?: return null
                    val tempFile = File.createTempFile("temp_", ".tmp", cacheDir)
                    FileOutputStream(tempFile).use { inputStream.copyTo(it) }
                    tempFile.absolutePath
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }
        return null
    }

    private fun updateProgress(progress: Int) {
        binding.seekBarProgress.progress = progress
        binding.tvProgressPercent.text = "${progress}%"
    }

    private fun showResultFiles(results: List<MediaFile>) {
        binding.toolbar.ivRightIcon2.gone()
        if (results.isEmpty()) return

        // Initialize RecyclerView if needed
        if (binding.rvFiles.adapter == null) {
            filesAdapter = MediaFileAdapter(true)
            binding.rvFiles.apply {
                adapter = filesAdapter
                layoutManager = LinearLayoutManager(context)
                visible()
            }
        }

        // Force visibility
        binding.rvFiles.visible()

        // Update adapter with new data
        filesAdapter?.submitList(null) // Clear previous items
        filesAdapter?.submitList(results) // Set new items

        // Force layout update
        binding.rvFiles.post {
            binding.rvFiles.invalidate()
            binding.root.requestLayout()
        }
    }

    suspend fun executeFfmpegAsyncBlocking(
        command: String,
        onStatistics: (com.arthenica.ffmpegkit.Statistics) -> Unit
    ): Session {
        return suspendCancellableCoroutine { cont ->
            FFmpegKit.executeAsync(
                command,
                { session -> cont.resume(session, null) },
                { /* log callback */ },
                { statistics -> onStatistics(statistics) }
            )
        }
    }



    private fun showErrorAndFinish() {
        Toast.makeText(
            this@SavingProgressActivity,
            getString(R.string.error_occurred_generic),
            Toast.LENGTH_SHORT
        ).show()
        finish()
    }

//    Cả hai đều là đường dẫn tuyệt đối.
//    inputPath → file gốc (thường trong thư mục chung của máy).
//    tempFile.absolutePath → file tạm/output (thường trong thư mục riêng của app).
    private fun handleSpeedConversion() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val results =
                    intent.getParcelableArrayListCompat<MediaFile>(EXTRA_RESULTS) ?: emptyList()
                val mediaFile = results.firstOrNull() ?: return@launch
                val speed = intent.getFloatExtra(EXTRA_SPEED, 1.0f)

                val tempFile = generateOutputFile(
                    nameType = "speedAudio",
                    outputDir = cacheDir,
                    extension = mediaFile.format?.value ?: FormatType.MP3.value
                )

                val inputPath = FileUtils.getPath(
                    this@SavingProgressActivity,
                    mediaFile.uri
                )
                    ?: throw Exception("Không đọc được file gốc")

                val duration = mediaFile.duration
                var lastProgress = -1

                val session = suspendCancellableCoroutine { continuation ->
                    FFmpegKit.executeAsync(
                        "-i \"$inputPath\" -filter:a atempo=$speed -vn \"${tempFile.absolutePath}\"",
                        { session ->
                            //                continuation.resume(session) {
//                    if (tempFile.exists())   tempFile.delete()
//                }
                            continuation.resumeWith(Result.success(session))
                            continuation.invokeOnCancellation {
                                if (tempFile.exists()) tempFile.delete()
                                lifecycleScope.launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@SavingProgressActivity,
                                        getString(R.string.error_occurred_generic),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    finish()
                                }
                            }
                        },
                        { log -> println("FFmpeg log: ${log.message}") },
                        { statistics ->
                            val timeInMs = statistics.time
                            if (duration > 0) {
                                val progress =
                                    ((timeInMs * 100) / duration).toInt().coerceIn(0, 100)
                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    lifecycleScope.launch(Dispatchers.Main) {
                                        updateProgress(progress)
                                    }
                                }
                            }
                        }
                    )
                }

                if (ReturnCode.isSuccess(session.returnCode)) {
                    val scanResult = MediaScanner.scanFile(
                        context = this@SavingProgressActivity,
                        tempFileFromFfmpeg = tempFile,
                        editType = EditType.AUDIO_SPEED,
                        newFileName = mediaFile.name.substringBeforeLast('.'),
                        newFileFormat = mediaFile.format ?: FormatType.MP3
                    )

                    if (scanResult != null) {
                        val (newMediaFile, _) = scanResult
                        val mediaEntityToInsert = newMediaFile.toMediaEntry()
                        val generatedId = mediaFileRepository.addMedia(mediaEntityToInsert)

                        startActivity(
                            ResultActivity.newIntent(
                                this@SavingProgressActivity,
                                newMediaFile.copy(id = generatedId)
                            )
                        )
                        finish()
                    }
                } else {
                    throw Exception("Error processing audio")
                }

            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this@SavingProgressActivity,
                        e.message ?: getString(R.string.error),
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }
        }
    }

    private fun getLocaleFromLanguage(name: String): Locale {
        return when (name) {
            "English" -> Locale.ENGLISH
            "Hindi" -> Locale("hi")
            "Arabic" -> Locale("ar")
            "Spanish" -> Locale("es")
            "Portuguese" -> Locale("pt")
            "French" -> Locale.FRENCH
            else -> Locale.ENGLISH
        }
    }
}
