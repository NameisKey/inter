package com.nameiskey.intermediate2.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.model.StoryList
import com.nameiskey.intermediate2.model.StoryResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel : ViewModel(), LifecycleObserver {
    private val _auth = MutableLiveData<String>()

    private val _story = MutableLiveData<List<StoryList>>()
    val story: LiveData<List<StoryList>> = _story

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseType = MutableLiveData<Int>()
    val responseType: LiveData<Int> = _responseType

    internal fun setAuth(auth: String) {
        _auth.value = auth
        showStories(_auth.value.toString())
    }

    private fun showStories(auth: String) {
        _isLoading.value = true
        _responseType.value = HttpResponseCode.SUCCESS

        val client = ApiConfig.getApiService().getStories("Bearer $auth", LOCATION_STATUS)

        client.enqueue(object : Callback<StoryResponse> {
            override fun onResponse(
                call: Call<StoryResponse>,
                response: Response<StoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        if (response.body()?.listStory?.isEmpty() == false) {
                            _story.value = response.body()?.listStory
                            _isLoading.value = false
                        } else {
                            _responseType.value = HttpResponseCode.NOT_FOUND
                            _isLoading.value = false
                        }
                    } else {
                        _responseType.value = HttpResponseCode.FAILED
                        _isLoading.value = false
                    }
                } else {
                    _responseType.value = HttpResponseCode.FAILED
                    _isLoading.value = false
                }
            }

            override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
                _responseType.value = HttpResponseCode.SERVER_ERROR
                _isLoading.value = false
            }
        })
    }

    companion object {
        private const val LOCATION_STATUS = 1
    }
}