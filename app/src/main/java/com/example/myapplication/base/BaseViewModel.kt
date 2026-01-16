package com.example.myapplication.base

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private var countLoading = 0

    protected fun <T> runAsync(
        request: suspend () -> ApiResult<T>,
        onSuccess: (T) -> Unit = {},
        onError: (ApiResult.Error) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = request()) {
                is ApiResult.Success -> onSuccess(result.data)
                is ApiResult.Error -> onError(result)
            }
        }
    }

    protected fun <T> runAsyncWithLoading(
        request: suspend () -> ApiResult<T>,
        onSuccess: (T) -> Unit = {},
        onError: (ApiResult.Error) -> Unit = {}
    ) {
        showLoading()
        viewModelScope.launch(Dispatchers.IO) {
            when(val result = request()) {
                is ApiResult.Success -> onSuccess(result.data)
                is ApiResult.Error -> onError(result)
            }
            hideLoading()
        }
    }

    protected fun showLoading() {
        synchronized(this) {
            countLoading++
            _isLoading.postValue(countLoading > 0)
        }
    }

    protected fun hideLoading() {
        synchronized(this) {
            countLoading--
            _isLoading.postValue(countLoading > 0)
        }
    }
}