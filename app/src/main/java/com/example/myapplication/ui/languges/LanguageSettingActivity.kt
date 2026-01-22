package com.example.myapplication.ui.languges

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.data.model.LanguageModel
import com.example.myapplication.databinding.ActivityLanguageSettingBinding
import com.example.myapplication.ui.languges.adapter.LanguageAdapter
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.tap

class LanguageSettingActivity :
    BaseActivity<ActivityLanguageSettingBinding>(ActivityLanguageSettingBinding::inflate) {

    private var languageAdapter: LanguageAdapter? = null
    private var lang: String = ""

    override fun initView() {
        super.initView()
        val listLanguage = mutableListOf<LanguageModel>()
        val listLang = listOf(
            LanguageModel("English", "en", false, R.drawable.ic_language_en),
            LanguageModel("Hindi", "hi", false, R.drawable.ic_language_hi),
            LanguageModel("Spanish", "es", false, R.drawable.ic_language_es),
            LanguageModel("French", "fr", false, R.drawable.ic_language_fr),
            LanguageModel("Portuguese", "pt", false, R.drawable.ic_language_pt),
            LanguageModel("German", "de", false, R.drawable.ic_language_de),
            LanguageModel("Japanese", "ja", false, R.drawable.ic_language_jp),
            LanguageModel("Korea", "ko", false, R.drawable.ic_language_ko),
        )

        listLanguage.addAll(listLang)

        val linearLayoutManager = LinearLayoutManager(this)

        val savedLang = SystemUtil.getCurrentLanguage(this)
        if (savedLang != null) {
            val selected = listLanguage.firstOrNull { it.code == savedLang }
            if (selected != null) {
                listLanguage.remove(selected)
                listLanguage.add(0, selected)
            }
        }


        languageAdapter =
            LanguageAdapter( listLanguage, object : LanguageAdapter.OnItemClickListener {
                override fun onItemClick(languageTag: String) {
                    lang = languageTag
                }
            })
        binding.rvLanguage.layoutManager = linearLayoutManager
        binding.rvLanguage.adapter = languageAdapter
        languageAdapter?.selectedItemPosition = 0

    }

    override fun bindView() {
        super.bindView()
        binding.clHeader.apply {
            tvTitle.text = getString(R.string.language)
            ivRightIcon2.isVisible = false
            ivRightIcon1.isVisible = true
            ivLeftIcon.tap {
                finish()
            }

            ivRightIcon1.setImageDrawable(
                ContextCompat.getDrawable(
                    this@LanguageSettingActivity,
                    R.drawable.ic_next_language
                )
            )

            ivRightIcon1.tap {
                SystemUtil.changeLanguage(this@LanguageSettingActivity, lang)
                nextActivityProcess()
            }
        }
    }

    private fun nextActivityProcess() {
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }
}