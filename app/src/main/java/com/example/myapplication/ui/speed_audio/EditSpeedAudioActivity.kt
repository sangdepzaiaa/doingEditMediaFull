package com.example.myapplication.ui.speed_audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.SeekBar
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.FormatType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.ActivityEditSpeedAudioBinding
import com.example.myapplication.ui.result.SavingProgressActivity
import com.example.myapplication.utils.const.EXTRA_RESULTS
import com.example.myapplication.utils.generateOutputFileName
import com.example.myapplication.utils.getParcelableExtraCompat
import com.example.myapplication.utils.gone
import com.example.myapplication.utils.tap
import java.util.Locale
import java.util.concurrent.TimeUnit

class EditSpeedAudioActivity :
    BaseActivity<ActivityEditSpeedAudioBinding>(inflater = ActivityEditSpeedAudioBinding::inflate) {

    private var mediaFile: MediaFile? = null
    private var outputFileName: String = ""

    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    private val speedSteps = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 1.75f, 2f)
    private var currentSpeed = 1f

    companion object {
        private const val EXTRA_MEDIA_FILE_SPEED_AUDIO = "EXTRA_MEDIA_FILE_SPEED_AUDIO"

        fun newIntent(context: Context, mediaFile: MediaFile): Intent =
            Intent(context, EditSpeedAudioActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_FILE_SPEED_AUDIO, mediaFile)
            }
    }

    override fun initView() {
        super.initView()
        mediaFile =
            intent.getParcelableExtraCompat(EXTRA_MEDIA_FILE_SPEED_AUDIO, MediaFile::class.java)
        if (mediaFile == null) {
            finish()
            return
        }
        initToolbar()
        initInfoMedia(mediaFile)
        setupMediaPlayer()
        setupPlayPauseButton()
        setupSpeedSeekBar()
        setupSaveButton()
        setupSeekBarTime()
    }

    private fun initInfoMedia(mediaFile: MediaFile?) {
        outputFileName = mediaFile?.name?.substringBeforeLast('.') ?: ""
        binding.txvnamefile.text = mediaFile?.name
        binding.txvdurationaudio.text = formatDuration(mediaFile?.duration ?: 0L)
        binding.tvProgress.text = formatDuration(mediaFile?.duration ?: 0L)
        binding.tvDuration.text =
            getString(R.string.duration_begin, formatDuration(mediaFile?.duration ?: 0L))
        // Initialize tvDurationedit with calculated duration based on current speed
        val calculatedDuration = ((mediaFile?.duration ?: 0L) / currentSpeed).toLong()
        binding.tvDurationedit.text = formatDuration(calculatedDuration)
        binding.txvmp3.text = getFileExtension(mediaFile?.name ?: "")
        binding.txvsizeaudio.text = formatSize(mediaFile?.size ?: 0L)
        binding.imgpauseaudiospeed.setImageResource(R.drawable.ic_play_square)
    }

    private fun initToolbar() {
        binding.toolBar.ivLeftIcon.tap { finish() }
        binding.toolBar.ivRightIcon2.gone()
        binding.toolBar.tvTitle.text = getString(R.string.audiospeed)
    }

    fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    private fun setupMediaPlayer() {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@EditSpeedAudioActivity, mediaFile?.uri ?: return)
            prepare()
        }
        binding.seekBarSpeed.max = mediaFile?.duration?.toInt() ?: 1
        mediaPlayer?.setOnCompletionListener {
            isPlaying = false
            binding.imgpauseaudiospeed.setImageResource(R.drawable.ic_play_square)
            binding.seekBarSpeed.progress = 0
            handler.removeCallbacks(updateSeekRunnable)
        }
    }

    private fun setupPlayPauseButton() {
        binding.imgpauseaudiospeed.tap {
            mediaPlayer?.let {
                if (isPlaying) {
                    it.pause()
                    isPlaying = false
                    binding.imgpauseaudiospeed.setImageResource(R.drawable.ic_play_square)
                    handler.removeCallbacks(updateSeekRunnable)
                } else {
                    it.playbackParams = it.playbackParams.setSpeed(currentSpeed)
                    it.start()
                    isPlaying = true
                    binding.imgpauseaudiospeed.setImageResource(R.drawable.ic_pause_square)
                    handler.post(updateSeekRunnable)
                }
            }
        }
    }


    private fun setupSeekBarTime() {
        binding.seekBarSpeed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                    // tvProgress shows current position adjusted to speed
                    binding.tvProgress.text = formatDuration((progress / currentSpeed).toLong())
                    // tvDuration always shows original total duration (unchanged)
                    binding.tvDuration.text = getString(
                        R.string.duration_begin,
                        formatDuration(mediaFile?.duration ?: 0L)
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSpeedSeekBar() {
        binding.seekBarSaveSpeed.max = speedSteps.size - 1
        binding.seekBarSaveSpeed.progress = speedSteps.indexOf(1f)
        currentSpeed = 1f

        binding.seekBarSaveSpeed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    currentSpeed = speedSteps[progress]

                    // Calculate new duration based on speed: faster speed = shorter duration
                    val originalDuration = mediaFile?.duration ?: 0L
                    val newDuration = (originalDuration / currentSpeed).toLong()
                    binding.tvDurationedit.text = formatDuration(newDuration)

                    if (isPlaying) {
                        // nếu đang phát thì đổi speed ngay
                        updateSpeed()
                    } else {
                        // Reset progress to start when changing speed while not playing
                        binding.tvProgress.text = getString(R.string.duration_result)
                        binding.seekBarSpeed.progress = 0
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    @SuppressLint("NewApi")
    private fun updateSpeed() {
        mediaPlayer?.let { player ->
            try {
                val params = player.playbackParams
                params.speed = currentSpeed
                player.playbackParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Calculate new duration based on speed: faster speed = shorter duration
            val originalDuration = mediaFile?.duration ?: 0L
            val newDuration = (originalDuration / currentSpeed).toLong()
            binding.tvDurationedit.text = formatDuration(newDuration)

            // cập nhật progress hiển thị theo speed
            val currentPos = player.currentPosition
            binding.tvProgress.text = formatDuration((currentPos / currentSpeed).toLong())
        }
    }

    private val updateSeekRunnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                val current = it.currentPosition
                // cập nhật seekbar luôn theo vị trí gốc (ms)
                binding.seekBarSpeed.progress = current
                // tvProgress shows current position adjusted by speed
                binding.tvProgress.text = formatDuration((current / currentSpeed).toLong())
                // tvDuration always shows original total duration (unchanged)
                binding.tvDuration.text =
                    getString(R.string.duration_begin, formatDuration(mediaFile?.duration ?: 0L))
                handler.postDelayed(this, 500)
            }
        }
    }

    private fun formatDuration(millis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun formatSize(bytes: Long): String {
        return if (bytes < 1024 * 1024) "${bytes / 1024} KB"
        else String.format(Locale.getDefault(), "%.1f MB", bytes / (1024.0 * 1024.0))
    }

    private fun setupSaveButton() {
        binding.btnSaveaudiospeed.tap {
            if (mediaFile == null) return@tap
            mediaPlayer?.pause()
            handler.removeCallbacks(updateSeekRunnable)

            val originalDuration = mediaFile?.duration ?: 0L
            val adjustedDuration = (originalDuration / currentSpeed).toLong()
            val format = mediaFile?.format ?: FormatType.MP3
            val extraResult = mediaFile!!.copy(
                name = generateOutputFileName("speedAudio", format),
                duration = adjustedDuration
            )
            val intent = SavingProgressActivity.newSpeedIntent(
                context = this,
                speed = currentSpeed
            )
            intent.putParcelableArrayListExtra(EXTRA_RESULTS, arrayListOf(extraResult))
            startActivity(intent)
            finish()
        }
    }

    private val screenOffReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                mediaPlayer?.pause()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(screenOffReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacks(updateSeekRunnable)
    }
}