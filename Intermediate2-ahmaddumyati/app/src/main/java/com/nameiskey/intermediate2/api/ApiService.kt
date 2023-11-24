package com.nameiskey.intermediate2.api

import com.nameiskey.intermediate2.model.AddStoryResponse
import com.nameiskey.intermediate2.model.DetailResponse
import com.nameiskey.intermediate2.model.LoginResponse
import com.nameiskey.intermediate2.model.RegisterResponse
import com.nameiskey.intermediate2.model.StoryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @GET("stories")
    fun getStories(
        @Header("Authorization") auth: String,
        @Query("location") location: Int? = 0
    ): Call<StoryResponse>

    @GET("stories")
    suspend fun getStoriesWithPage(
        @Header("Authorization") auth: String,
        @Query("location") location: Int? = 0,
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = 5
    ): StoryResponse

    @GET("stories/{id}")
    fun getStoryDetail(
        @Header("Authorization") auth: String,
        @Path("id") id: String
    ): Call<DetailResponse>

    @Multipart
    @POST("stories")
    fun addNewStory(
        @Header("Authorization") auth: String,
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody?,
        @Part("lon") lon: RequestBody?
    ): Call<AddStoryResponse>
}