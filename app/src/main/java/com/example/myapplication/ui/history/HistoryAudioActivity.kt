package com.example.myapplication.ui.history

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityHistoryAudioBinding
import com.example.myapplication.ui.dialog.DialogHelper
import com.example.myapplication.ui.result.ResultActivity
import com.example.myapplication.ui.result.ResultViewModel
import com.example.myapplication.utils.deleteFile
import com.example.myapplication.utils.showToast
import com.example.myapplication.utils.tap
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import kotlin.collections.get
import kotlin.collections.isNullOrEmpty
import kotlin.getValue
import kotlin.text.get
import kotlin.text.set

class HistoryAudioActivity :
    BaseActivity<ActivityHistoryAudioBinding>(ActivityHistoryAudioBinding::inflate) {

    private val vm: HistoryViewmodel by inject()
    private val resultViewmodel: ResultViewModel by inject()

    private val recyclerViews by lazy {
        mapOf(
            0 to binding.rvall,
            1 to binding.rvaudiospeed,
            2 to binding.rvvideotoaudio,
        )
    }

    private val adapters by lazy {
        (0..2).associateWith { MediaFileAdapter() }
    }

    // Map LiveData với adapter tương ứng
    private val liveDataAdapterMap by lazy {
        mapOf(
            vm.allHistory to adapters[0],
            vm.audioSpeed to adapters[1],
            vm.videoToAudio to adapters[2],
//            vm.audioMerge to adapters[3],
//            vm.audioMixer to adapters[4],
//            vm.audioConvert to adapters[5],
//            vm.audioVolume to adapters[6],
//            vm.voiceChange to adapters[7],
//            vm.textToAudio to adapters[8],
//            vm.audioSpeed to adapters[9]
        )
    }

    override fun initView() {
        super.initView()
        binding.toolbar.ivLeftIcon.tap { finish() }

        setupRecyclerViews()
        setupAdapterListeners()
        observeViewModel()

        // load dữ liệu từ DB
        vm.loadHistory()

        // hiển thị tab đầu tiên
        showTab(0)

        // sự kiện đổi tab
        setupTabLayout()
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { showTab(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun observeViewModel() {
        liveDataAdapterMap.forEach { (liveData, adapter) ->
            liveData.observe(this) {
                adapter?.submitList(it)
                updateEmptyViewVisibility()
            }
        }
    }

    private fun setupAdapterListeners() {
        adapters.values.forEach { adapter ->
            adapter.setonItemClickListener { media ->
                val intent = ResultActivity.newIntent(this@HistoryAudioActivity, media, true)
                startActivity(intent)
            }
            adapter.setOnMoreListener { pair ->
                DialogHelper.showPopupHistory(
                    anchor = pair.second,
                    context = this,
                    curentMediaFile = pair.first,
                    onRename = { newName ->
                        lifecycleScope.launch {
                            val loadingDialog = DialogHelper.SavingDialog(this@HistoryAudioActivity)
                            loadingDialog.show()

                            resultViewmodel.updateNameMediaFile(pair.first.id, newName)
                            updateMediaFileInAdapters(pair.first.id, newName)
                            vm.loadHistory()

                            loadingDialog.dismiss()
                            showToast(getString(R.string.rename_success))
                        }
                    },
                    onItemDelete = {
                        lifecycleScope.launch {
                            try {
                                resultViewmodel.deleteMediaFile(it.id)
                                this@HistoryAudioActivity.deleteFile(it.uri)
                                vm.loadHistory() // reload data
                                showToast(getString(R.string.delete))
                            } catch (e: Exception) {

                            }
                        }
                    }
                )
            }
        }
    }

    private fun updateMediaFileInAdapters(mediaFileId: Long, newName: String) {
        adapters.values.forEach { adapter ->
            val currentList = adapter.currentList.toMutableList()
            val index = currentList.indexOfFirst { it.id == mediaFileId }
            if (index != -1) {
                currentList[index] = currentList[index].copy(name = newName)
                adapter.submitList(null)
                adapter.submitList(currentList)
            }
        }
    }

    private fun setupRecyclerViews() {
        recyclerViews.forEach { (index, rv) ->
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = adapters[index]
        }
    }

    private fun showTab(position: Int) {
        recyclerViews.values.forEach { it.visibility = View.GONE }
        recyclerViews[position]?.visibility = View.VISIBLE
        updateEmptyViewVisibility()
    }

    private fun updateEmptyViewVisibility() {
        val currentTabPosition = binding.tabLayout.selectedTabPosition
        val currentLiveData = liveDataAdapterMap.keys.elementAtOrNull(currentTabPosition)
        val currentList = currentLiveData?.value

        binding.viewEmptyItem.root.visibility =
            if (currentList?.isEmpty() == true) {
                View.VISIBLE
            } else {
                View.GONE
            }

    }
}