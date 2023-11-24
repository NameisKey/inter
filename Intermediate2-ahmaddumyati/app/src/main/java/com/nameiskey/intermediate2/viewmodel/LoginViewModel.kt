package com.nameiskey.intermediate2.viewmodel

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.model.LoginResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel(), LifecycleObserver {
    private val _user = MutableLiveData<LoginResponse>()
    val user: LiveData<LoginResponse> = _user

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseType = MutableLiveData<Int>()
    val responseType: LiveData<Int> = _responseType

    internal fun userLogin(email: String, password: String) {
        _isLoading.value = true
        _responseType.value = HttpResponseCode.SUCCESS

        val client = ApiConfig.getApiService().login(email, password)

        client.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(
                call: Call<LoginResponse>,
                response: Response<LoginResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null) {
                        _user.value = response.body()
                        _responseType.value = HttpResponseCode.SUCCESS
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

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _responseType.value = HttpResponseCode.SERVER_ERROR
                _isLoading.value = false
            }
        })
    }
}