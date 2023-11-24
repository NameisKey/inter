package com.nameiskey.intermediate2.viewmodel

import android.location.Location
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.nameiskey.intermediate2.api.ApiConfig
import com.nameiskey.intermediate2.model.AddStoryResponse
import com.nameiskey.intermediate2.util.HttpResponseCode
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class AddStoryViewModel: ViewModel(), LifecycleObserver {
    private val _currentPhotoPath = MutableLiveData<String>()
    val currentPhotoPath: LiveData<String> = _currentPhotoPath

    private val _file = MutableLiveData<File>()
    val file: LiveData<File> = _file

    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location> = _location

    private val _auth = MutableLiveData<String>()

    private val _story = MutableLiveData<AddStoryResponse>()
    val story: LiveData<AddStoryResponse> = _story

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _responseType = MutableLiveData<Int>()
    val responseType: LiveData<Int> = _responseType

    internal fun setPhotoPath(photoPath: String) {
        _currentPhotoPath.value = photoPath
    }

    internal fun setFile(file: File) {
        _file.value = file
    }

    internal fun setLocation(location: Location) {
        _location.value = location
    }

    internal fun setAuth(
        auth: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ) {
        _auth.value = auth
        uploadStory(_auth.value.toString(), description, photo, lat, lon)
    }

    private fun uploadStory(
        auth: String,
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody?,
        lon: RequestBody?
    ) {
        _isLoading.value = true
        _responseType.value = HttpResponseCode.SUCCESS

        val client =
            ApiConfig.getApiService().addNewStory("Bearer $auth", description, photo, lat, lon)

        client.enqueue(object : Callback<AddStoryResponse> {
            override fun onResponse(
                call: Call<AddStoryResponse>,
                response: Response<AddStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody != null && !responseBody.error) {
                        _story.value = response.body()
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

            override fun onFailure(call: Call<AddStoryResponse>, t: Throwable) {
                _responseType.value = HttpResponseCode.SERVER_ERROR
                _isLoading.value = false
            }
        })
    }
}