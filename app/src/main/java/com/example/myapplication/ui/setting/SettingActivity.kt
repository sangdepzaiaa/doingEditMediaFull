package com.example.myapplication.ui.setting

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivitySettingBinding
import com.example.myapplication.ui.dialog.DialogFeedback
import com.example.myapplication.ui.dialog.RatingDialog
import com.example.myapplication.ui.languges.LanguageSettingActivity
import com.example.myapplication.utils.SharePreUtils
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.const
import com.example.myapplication.utils.tap
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Dispatcher
import java.io.File

class SettingActivity : BaseActivity<ActivitySettingBinding>
    (ActivitySettingBinding::inflate) {

   // private var remoteConfigScreenFeedbackModel: RemoteConfigScreenFeedbackModel? = null
    private var check = false

    override fun initView() {
        super.initView()
        binding.toolbar.apply {
            tvTitle.text = getString(R.string.setting)
            ivRightIcon2.visibility = View.GONE
        }
    }
    private fun feedBack() {
        val dialogFeedback = DialogFeedback()
        dialogFeedback.show(supportFragmentManager, "dialogfeeback")
    }

    fun adjustLayout() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_000000_7)
    }


    private fun showRateDialogNew() {
        val ratingDialog = RatingDialog(this)
        ratingDialog.init(object : RatingDialog.OnPress {
            override fun send(s: Int) {
                SharePreUtils.forceRated(this@SettingActivity)

            }

            override fun rating(s: Int) {
                SharePreUtils.forceRated(this@SettingActivity)
                onRateAppNew()
            }

            override fun later() {
                ratingDialog.dismiss()
            }

            override fun gotIt() {
                ratingDialog.dismiss()
            }

            override fun cancel() {
                ratingDialog.dismiss()
            }

        })
        ratingDialog.show()
        ratingDialog.setOnDismissListener {
            check = false
            if (SharePreUtils.isRated(this@SettingActivity)) {
             //   binding.clRating.visibility = View.GONE
            }

        }
    }

    private fun onRateAppNew() {
        var reviewInfo: ReviewInfo?
        val manager = ReviewManagerFactory.create(this)
        val request: Task<ReviewInfo> = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                SharePreUtils.forceRated(this)
                reviewInfo = task.result
                val flow: Task<Void> =
                    manager.launchReviewFlow(this, reviewInfo!!)
                flow.addOnSuccessListener {
                    rateAppOnStoreNew()
                }
            }
        }
    }

    private fun rateAppOnStoreNew() {
        val packageName = baseContext.packageName
        val uri: Uri = "market://details?id=$packageName".toUri()
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "http://play.google.com/store/apps/details?id=$packageName".toUri()
                )
            )
        }

    }

    override fun onResume() {
        super.onResume()
        check = false
        //SystemUtil.setLocale(this)
        if (SharePreUtils.isRated(this)) {
           // binding.clRating.visibility = View.VISIBLE
        }
        //re enable appopen resume ad
        //AppOpenResumeManager.setEnableAdsResume(AppOpenResumeManager.getIsShow())
    }

    override fun bindView() {
        super.bindView()
        adjustLayout()

        binding.toolbar.ivLeftIcon.tap {
            finish()
        }

        binding.clRating.tap {
            if (!check) {
                check = true
                showRateDialogNew()
                resetCheck()
            }
        }

        binding.clLanguage.tap {
            if (!check) {
                check = true
                val intent = Intent(this, LanguageSettingActivity::class.java)
                startActivity(intent)
                resetCheck()
            }
        }

//        ACTION_VIEW + https://...
//        Android hiểu:
//        “Mở URL này bằng app phù hợp”
//        ➡️ Thường là:
//        Chrome
//        Browser mặc định
//        WebView app khác
        binding.clPolicy.tap {
            if (!check) {
                check = true
                val intent = Intent(Intent.ACTION_VIEW, const.POLICY_URL.toUri())
                startActivity(intent)
                resetCheck()
                //disable ad resume for policy
                //AppOpenResumeManager.setEnableAdsResume(false)
            }
        }

        binding.clShare.tap {
            if (!check) {
                check = true
                share()
                resetCheck()
            }
        }

        binding.clFeedback.tap {
            if (!check) {
                check = true
                feedBack()
                resetCheck()
            }
        }
    }





    //ACTION_SEND = hành động gửi dữ liệu
    //Android hiểu:
    //“Có app nào nhận dữ liệu từ app này không?”
    var imageFile: File ? = null
    private fun share() {

        val uri = FileProvider.getUriForFile(
            this@SettingActivity,
            "${packageName}.provider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "${getString(R.string.app_name)}\nhttps://play.google.com/store/apps/details?id=$packageName"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        startActivity(
            Intent.createChooser(intent, getString(R.string.share))
        )
    }




    fun resetCheck() {
        lifecycleScope.launch(Dispatchers.Main) {
            delay(1000)
            check = false
        }
    }
}
