package com.autoqr.network

import com.autoqr.model.LoginRequest
import com.autoqr.model.RegisterRequest
import com.autoqr.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<Void>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<TokenResponse>



}
