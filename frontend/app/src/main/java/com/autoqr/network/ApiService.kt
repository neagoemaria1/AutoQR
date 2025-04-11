package com.autoqr.network

import com.autoqr.model.LoginRequest
import com.autoqr.model.RegisterRequest
import com.autoqr.model.SendMessageRequest
import com.autoqr.model.TokenResponse
import com.autoqr.model.UserProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>


    @POST("api/notification/token")
    suspend fun updateDeviceToken(
        @Header("Authorization") token: String,
        @Body deviceToken: String
    ): Response<Void>


    @POST("api/message/send")
    suspend fun sendMessage(
        @Header("Authorization") token: String,
        @Body request: SendMessageRequest
    ): Response<Void>
}
