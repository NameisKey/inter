package com.nameiskey.intermediate2.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.model.DetailResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailViewModel  : ViewModel(), LifecycleObserver {
    private val _auth = MutableLiveData<String>()

    private val _detail = MutableLiveData<DetailResponse>()
    val detail: LiveData<DetailResponse> = _detail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseType = MutableLiveData<Int>()
    val responseType: LiveData<Int> = _responseType

    internal fun setAuth(auth: String, id: String) {
        _auth.value = auth
        showStoryDetail(_auth.value.toString(), id)
    }

    private fun showStoryDetail(auth: String, id: String) {
        _isLoading.value = true
        _responseType.value = HttpResponseCode.SUCCESS

        val client = ApiConfig.getApiService().getStoryDetail("Bearer $auth", id)

        client.enqueue(object : Callback<DetailResponse> {
            override fun onResponse(
                call: Call<DetailResponse>,
                response: Response<DetailResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _detail.value = response.body()
                        _isLoading.value = false
                    } else {
                        _responseType.value = HttpResponseCode.FAILED
                        _isLoading.value = false
                    }
                } else {
                    _responseType.value = HttpResponseCode.FAILED
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                _responseType.value = HttpResponseCode.SERVER_ERROR
                _isLoading.value = false
            }
        })
    }
}