package com.example.myapplication.ui.result

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.extractor.DefaultExtractorsFactory
import androidx.media3.extractor.ts.AdtsExtractor
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.enumm.EditType
import com.example.myapplication.data.enumm.MediaFile
import com.example.myapplication.databinding.ActivityResultBinding
import com.example.myapplication.ui.dialog.DialogHelper
import com.example.myapplication.ui.dialog.DialogHelper.showPopupResult
import com.example.myapplication.ui.dialog.DialogHelper.showRenameDialog
import com.example.myapplication.ui.home.HomeActivity
import com.example.myapplication.utils.const
import com.example.myapplication.utils.const.EXTRA_MEDIA_FILE
import com.example.myapplication.utils.const.IS_FROM_HISTORY
import com.example.myapplication.utils.const.MAX_TOTAL_SELECTION_DURATION_MS
import com.example.myapplication.utils.const.MIN_SELECTED_MEDIA_DURATION_MS
import com.example.myapplication.utils.deleteFile
import com.example.myapplication.utils.formatDuration
import com.example.myapplication.utils.formatFileSize
import com.example.myapplication.utils.getParcelableExtraCompat
import com.example.myapplication.utils.share
import com.example.myapplication.utils.showToast
import com.example.myapplication.utils.tap
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class ResultActivity :
    BaseActivity<ActivityResultBinding>(inflater = ActivityResultBinding::inflate) {
    private val viewModel: ResultViewModel by inject()
    private var mediaFile: MediaFile? = null
    private var exoPlayer: ExoPlayer? = null
    private var isPlaying = false
    private var isPrepared = false

    // Seekbar handling variables like AudioBoosterActivity
    private var isUserSeeking = false
    private var userSeekPosition = 0L
    private var isAudioCompleted = false
    private var lastKnownPosition = 0L

    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val progressUpdateRunnable = Runnable { updateProgress() }

    // Activity Result Launcher for contact picker
//    private val contactPickerLauncher: ActivityResultLauncher<Intent> =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            try {
//                if (result.resultCode == RESULT_OK && result.data != null) {
//                    result.data?.data?.let { contactUri ->
//                        handleContactSelected(contactUri)
//                    }
//                }
//            } catch (e: Exception) {
//                showToast(getString(R.string.failed_to_set_contact_ringtone))
//            }
//        }

    companion object {
        private const val UPDATE_INTERVAL = 250L // Match AudioBoosterActivity
        private const val END_THRESHOLD = 500L
        private const val SEEK_DELAY = 50L

        fun newIntent(context: Context, mediaFile: MediaFile, isFromHistory: Boolean? = false): Intent =
            Intent(context, ResultActivity::class.java).apply {
                putExtra(EXTRA_MEDIA_FILE, mediaFile)
                putExtra(IS_FROM_HISTORY, isFromHistory)
            }
    }

    private val playerListener = object : Player.Listener {
        @SuppressLint("SwitchIntDef")
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    isPrepared = true
                    setupSeekBarMax()
                    if (!isUserSeeking && !isAudioCompleted) {
                        resetSeekBarPosition()
                    }
                }

                Player.STATE_ENDED -> handlePlaybackEnded()
                Player.STATE_IDLE -> isPrepared = false
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            this@ResultActivity.isPlaying = isPlaying
            updatePlayPauseIcon()

            if (isPlaying && !isUserSeeking) {
                isAudioCompleted = false
                startProgressUpdates()
            } else {
                stopProgressUpdates()
            }
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            handlePlayerError(error)
        }
    }

    private fun handlePlayerError(error: androidx.media3.common.PlaybackException) {
        isPrepared = false
        isPlaying = false
        updatePlayPauseIcon()
        stopProgressUpdates()

        when (error.errorCode) {
            androidx.media3.common.PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND, androidx.media3.common.PlaybackException.ERROR_CODE_IO_NO_PERMISSION -> {
                handleInaccessibleFile()
            }

            else -> {
                Log.e("TAG", "handlePlayerError: ")
            }
        }
    }

    private fun handleInaccessibleFile() {
        lifecycleScope.launch {
            if (mediaFile?.id == 0L) return@launch
            try {
                viewModel.deleteMediaFile(mediaFile!!.id)
                finish()
            } catch (e: Exception) {
                finish()
            }
        }
    }

    override fun initView() {
        super.initView()
        loadMediaFile()
        setupClickListeners()
        setupSeekBar()
    }

    private fun loadMediaFile() {
        mediaFile =
            intent.getParcelableExtraCompat(EXTRA_MEDIA_FILE, MediaFile::class.java)

        if (mediaFile == null) {
            finish()
            return
        }
        setupUI()
        initPlayer()
    }

    private fun setupUI() {
        binding.apply {
            txvnamefile.text = mediaFile?.name
            txvdurationaudio.text = mediaFile?.duration?.formatDuration()
            txvsizeaudio.text = mediaFile?.size?.formatFileSize()
            txvmp3.text = mediaFile?.format?.value?.uppercase() ?: getString(R.string.unknown)
            toolbar.ivRightIcon1.visibility = View.GONE
            toolbar.ivRightIcon2.visibility = View.GONE

            // if isFromHistory, force title to "Detail", else depend on edit type
            val isFromHistory = intent.getBooleanExtra(IS_FROM_HISTORY, false)
            if (isFromHistory)
                toolbar.tvTitle.text = getString(R.string.detail)
            else {
                toolbar.tvTitle.text =
                    if (mediaFile?.editType == EditType.AUDIO_CONVERTER || mediaFile?.editType == EditType.VIDEO_TO_AUDIO) getString(
                        R.string.detail
                    )
                    else getString(R.string.result)
            }

            btnPlay.imgItem1.setImageResource(R.drawable.play_result)
            btnPlay.tvItem1.text = getString(R.string.play)
            btnMore.imgItem1.setImageResource(R.drawable.more)
            btnMore.tvItem1.text = getString(R.string.more)
            btnHome.imgItem1.setImageResource(R.drawable.home_result)
            btnHome.tvItem1.text = getString(R.string.home)
            btnShare.imgItem1.setImageResource(R.drawable.share)
            btnShare.tvItem1.text = getString(R.string.share)
            btnRingtone.imgItem1.setImageResource(R.drawable.music_result)
            btnRingtone.tvItem1.text = getString(R.string.ringtone)
            btnAlarm.imgItem1.setImageResource(R.drawable.alarm)
            btnAlarm.tvItem1.text = getString(R.string.alarm)
            btnNotification.imgItem1.setImageResource(R.drawable.notifition_result)
            btnNotification.tvItem1.text = getString(R.string.notification)
            btnContact.imgItem1.setImageResource(R.drawable.contract_result)
            btnContact.tvItem1.text = getString(R.string.contract)
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            toolbar.ivLeftIcon.tap { finish() }

            btnPlay.root.tap { togglePlayPause() }
            btnMore.root.tap {
                showPopupResult(anchor = btnMore.root, context = this@ResultActivity, onRename = {
                    showRenameDialog(
                        this@ResultActivity,
                        currentName = mediaFile?.name ?: "",
                        onSaveComplete = null,
                        onRename = { newName ->
                            renameMediaFile(newName)
                        })
                }, deleteMediaFile = {
                    DialogHelper.showDeleteDialog(this@ResultActivity) {
                        deleteMediaFile()
                    }
                }
                    , onCutter = {
                    if (mediaFile == null) return@showPopupResult

                    if ((mediaFile?.duration ?: 0L) < MIN_SELECTED_MEDIA_DURATION_MS) {
                        showToast(getString(R.string.please_select_a_media_file_at_least_5_seconds_long))
                        return@showPopupResult
                    }

                    if ((mediaFile?.duration ?: 0L) > MAX_TOTAL_SELECTION_DURATION_MS) {
                        showToast(getString(R.string.please_select_audio_files_with_a_total_duration_of_up_to_30_minutes))
                        return@showPopupResult
                    }
            //        navigationToCutAudio(mediaFile)
                })
            }
            btnHome.root.tap { navigationToHome() }
            btnShare.root.tap {
                exoPlayer?.let {
                    if (isPlaying) {
                        it.pause()
                    }
                }
                mediaFile!!.share(this@ResultActivity)
            }
//            btnRingtone.root.tap {
//                mediaFile?.let {
//                    PermissionUtils.checkAndRequestAllPermissions(
//                        this@ResultActivity, PermissionType.RINGTONES.value
//                    ) {
//                        RingtoneUtils.setAsRingtone(this@ResultActivity, it)
//                    }
//                }
//            }
//            btnAlarm.root.tap {
//                mediaFile?.let {
//                    PermissionUtils.checkAndRequestAllPermissions(
//                        this@ResultActivity, PermissionType.ALARMS.value
//                    ) {
//                        RingtoneUtils.setAsAlarm(this@ResultActivity, it)
//                    }
//                }
//            }
//            btnNotification.root.tap {
//                mediaFile?.let {
//                    PermissionUtils.checkAndRequestAllPermissions(
//                        this@ResultActivity, PermissionType.NOTIFICATIONS.value
//                    ) {
//                        RingtoneUtils.setAsNotification(this@ResultActivity, it)
//                    }
//                }
//            }
//            btnContact.root.tap {
//                PermissionUtils.checkAndRequestAllPermissions(
//                    this@ResultActivity, PermissionType.CONTACTS.value
//                ) {
//                    launchContactPicker()
//                }
//            }
        }
    }

    private fun navigationToHome() {
        val intent = Intent(this@ResultActivity, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupSeekBar() {
        binding.seekBarSpeed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    userSeekPosition = progress.toLong()
                    updateProgressText(userSeekPosition)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
                stopProgressUpdates()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                handler.postDelayed({
                    exoPlayer?.let { player ->
                        if (isPrepared) {
                            val seekPosition = userSeekPosition.coerceIn(0, player.duration)

                            if (seekPosition < player.duration - END_THRESHOLD) {
                                isAudioCompleted = false
                            }

                            player.seekTo(seekPosition)
                            lastKnownPosition = seekPosition

                            if (isAudioCompleted && seekPosition < player.duration - END_THRESHOLD) {
                                isAudioCompleted = false
                                if (!player.isPlaying) {
                                    player.play()
                                }
                            }
                        }
                    }

                    handler.postDelayed({
                        isUserSeeking = false
                        if (isPlaying && !isAudioCompleted) {
                            startProgressUpdates()
                        }
                    }, SEEK_DELAY)
                }, SEEK_DELAY)
            }
        })
    }

    @OptIn(UnstableApi::class)
    private fun initPlayer() {
        releasePlayer()
        if (mediaFile?.uri == null) {
            return
        }
        val extractorsFactory =
            if (mediaFile!!.format?.name?.equals("aac", ignoreCase = true) == true) {
                DefaultExtractorsFactory().setAdtsExtractorFlags(AdtsExtractor.FLAG_ENABLE_CONSTANT_BITRATE_SEEKING)
            } else {
                DefaultExtractorsFactory()
            }
        val mediaSourceFactory = DefaultMediaSourceFactory(this, extractorsFactory)

        try {
            exoPlayer =
                ExoPlayer.Builder(this).setMediaSourceFactory(mediaSourceFactory).build().apply {
                    val mediaItem = MediaItem.fromUri(mediaFile!!.uri)
                    setMediaItem(mediaItem)
                    addListener(playerListener)
                    prepare()
                }
        } catch (e: Exception) {
        }
    }

    private fun setupSeekBarMax() {
        exoPlayer?.let { player ->
            if (player.duration > 0) {
                binding.seekBarSpeed.max = player.duration.toInt()
            }
        }
    }

    private fun resetSeekBarPosition() {
        binding.apply {
            seekBarSpeed.progress = 0
            tvProgress.text = getString(R.string.formart) + 0L.formatDuration()
        }
        lastKnownPosition = 0L
    }

    private fun handlePlaybackEnded() {
        isAudioCompleted = true
        isPlaying = false
        stopProgressUpdates()
        updatePlayPauseIcon()

        exoPlayer?.let { player ->
            lastKnownPosition = player.duration
            binding.apply {
                seekBarSpeed.progress = player.duration.toInt()
                tvProgress.text =
                    getString(R.string.formart) + player.duration.formatDuration()

            }
        }
    }

    private fun togglePlayPause() {
        exoPlayer?.let { player ->
            if (!isPrepared) return

            try {
                if (isPlaying) {
                    player.pause()
                } else {
                    if (isAudioCompleted) {
                        player.seekTo(0)
                        isAudioCompleted = false
                        resetSeekBarPosition()
                    }
                    player.play()
                }
            } catch (e: Exception) {
                showToast("Error controlling playback")
            }
        }
    }

    private fun updatePlayPauseIcon() {
        val iconRes = if (isPlaying) R.drawable.ic_pause_circle else R.drawable.play_result
        binding.btnPlay.imgItem1.setImageResource(iconRes)
    }

    private fun startProgressUpdates() {
        if (!isUserSeeking && !isAudioCompleted) {
            stopProgressUpdates()
            handler.post(progressUpdateRunnable)
        }
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressUpdateRunnable)
    }

    private fun updateProgress() {
        if (isUserSeeking || isAudioCompleted) return

        exoPlayer?.let { player ->
            try {
                val currentPosition = player.currentPosition
                val duration = player.duration

                // Only update UI if position changed significantly to avoid jerking
                val positionDiff = kotlin.math.abs(currentPosition - lastKnownPosition)
                if (positionDiff > 300) {
                    handler.post {
                        binding.apply {
                            seekBarSpeed.progress = currentPosition.toInt()
                            tvProgress.text = getString(R.string.format_duration, currentPosition.formatDuration())

                        }
                    }
                    lastKnownPosition = currentPosition
                }

                if (currentPosition >= duration - END_THRESHOLD) {
                    stopProgressUpdates()
                } else if (isPlaying) {
                    scheduleProgressUpdate()
                }
            } catch (e: Exception) {
                stopProgressUpdates()
            }
        }
    }

    private fun scheduleProgressUpdate() {
        handler.postDelayed(progressUpdateRunnable, UPDATE_INTERVAL)
    }

//    private fun navigationToCutAudio(mediaFile: MediaFile?) {
//        if (mediaFile == null) return
//
//        startActivity(
//            CutAudioActivity.newIntent(
//                this@ResultActivity, mediaFile, EditType.AUDIO_CUTTER
//            )
//        )
//        initPlayer()
//        viewBinding.btnPlay.imgItem1.setImageResource(R.drawable.play_result)
//    }


    private fun renameMediaFile(newName: String) {
        lifecycleScope.launch {
            if (mediaFile?.id == 0L) {
                return@launch
            }
            try {
                viewModel.updateNameMediaFile(mediaFile!!.id, newName)
                mediaFile = mediaFile?.copy(name = newName)
                binding.txvnamefile.text = newName

                // Always return updated media file to calling activity
                mediaFile?.let { updatedFile ->
                    val resultIntent = Intent().apply {
                        putExtra(const.EXTRA_MEDIA_FILES, updatedFile)
                    }
                    setResult(RESULT_OK, resultIntent)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private fun deleteMediaFile() {
        lifecycleScope.launch {
            if (mediaFile?.id == 0L) {
                return@launch
            }
            try {
                viewModel.deleteMediaFile(mediaFile!!.id)
                mediaFile?.uri?.let { uri ->
                    val deleted = deleteFile(uri)
                    Log.d("DeleteFile", "File deletion result: $deleted")
                }

                // Always return deleted media file info to calling activity
                mediaFile?.let { deletedFile ->
                    val resultIntent = Intent().apply {
                        putExtra(const.EXTRA_MEDIA_FILES, deletedFile)
                        putExtra(const.ACTION_DELETE, true)
                    }
                    setResult(RESULT_OK, resultIntent)
                }

                finish()
            } catch (e: Exception) {
                finish()
            }
        }
    }

    private fun releasePlayer() {
        stopProgressUpdates()
        exoPlayer?.let { player ->
            try {
                player.removeListener(playerListener)
                player.release()
            } catch (e: Exception) {
                // Ignore release errors
            }
        }
        exoPlayer = null
        isPrepared = false
        isPlaying = false
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        PermissionUtils.handlePermissionResult(this, requestCode, grantResults)
//
//    }

    private fun updateProgressText(positionMs: Long) {
        binding.tvProgress.text = getString(
            R.string.format_duration, positionMs.formatDuration()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.let {
            if (isPlaying) it.pause()
        }
    }

//    private fun launchContactPicker() {
//        try {
//            val intent = RingtoneUtils.getContactPickerIntent()
//            contactPickerLauncher.launch(intent)
//        } catch (e: Exception) {
//            showToast(getString(R.string.cannot_open_contacts))
//        }
//    }
//
//    private fun handleContactSelected(contactUri: Uri) {
//        mediaFile?.let { mediaFile ->
//            val success = RingtoneUtils.setContactRingtone(this, contactUri, mediaFile)
//            if (!success) {
//                showToast(getString(R.string.failed_to_set_contact_ringtone))
//            }
//        }
//    }
}