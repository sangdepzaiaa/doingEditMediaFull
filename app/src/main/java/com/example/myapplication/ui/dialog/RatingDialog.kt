package com.example.myapplication.ui.dialog

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import com.example.myapplication.R
import com.example.myapplication.base.BaseDialog
import com.example.myapplication.databinding.DialogRatingBinding
import com.example.myapplication.utils.tap

class RatingDialog(context: Context) :
    BaseDialog<DialogRatingBinding>(
        context,
        inflater = DialogRatingBinding::inflate) {
    private var onPress: OnPress? = null
    private var s = 5


    override fun initView() {
        super.initView()
        setContentView(binding.root)
        val attributes = window?.attributes
        attributes?.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.let {
            it.attributes = attributes
            it.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        binding.ratingBar.rating = 5.0f  // 5 ngôi sao được chọn , tô vàng
        onclick()
        changeRating()
    }

    interface OnPress {
        fun send(s: Int)

        fun rating(s: Int)

        fun cancel()

        fun later()

        fun gotIt()
    }

    fun init(onPress: OnPress?) {
        this.onPress = onPress
    }

    // rating : số sao chọn
    //fromUser : người dùng có thay đổi hay không
    private fun changeRating() {
        binding.ratingBar.setOnRatingBarChangeListener { _, rating, fromUser ->
            if (!fromUser) return@setOnRatingBarChangeListener
            s = rating.toInt()
        }
    }

    private fun onclick() {
        binding.btnSubmit.tap {
            Log.d("TAG23", "onclick: ${binding.ratingBar.rating}")
            if (binding.ratingBar.rating <= 4.0) {
                onPress.let {
                    it?.send(s)
                }
            } else {
                onPress.let {
                    it?.rating(s)
                }
            }
            binding.tvTitle.text = context.getString(R.string.rating)
            binding.tvContent.text =
                context.getString(R.string.thank_you_for_taking_the_time_to_rate_us_i_m_really_appreciate_that)
            binding.btnGotit.visibility = View.VISIBLE
            binding.ratingBar.visibility = View.GONE
        }

        binding.btnCancel.tap {
            onPress.let {
                it?.later()
            }
        }

        binding.btnGotit.tap {
            onPress.let {
                it?.gotIt()
            }
        }
    }
}