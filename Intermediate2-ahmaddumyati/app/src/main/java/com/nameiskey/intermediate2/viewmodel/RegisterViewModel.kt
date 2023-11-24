package com.nameiskey.intermediate2.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.model.RegisterResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel  : ViewModel(), LifecycleObserver {
    private val _user = MutableLiveData<RegisterResponse>()
    val user: LiveData<RegisterResponse> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseType = MutableLiveData<Int>()
    val responseType: LiveData<Int> = _responseType

    internal fun userRegister(name: String, email: String, password: String) {
        _isLoading.value = true
        _responseType.value = HttpResponseCode.SUCCESS

        val client = ApiConfig.getApiService().register(name, email, password)

        client.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _user.value = response.body()
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

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                _responseType.value = HttpResponseCode.SERVER_ERROR
                _isLoading.value = false
            }
        })
    }
}