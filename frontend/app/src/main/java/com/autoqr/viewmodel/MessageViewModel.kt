// File: MessageViewModel.kt
package com.autoqr.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autoqr.model.SendMessageRequest
import com.autoqr.network.ApiClient
import com.autoqr.network.ApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MessageViewModel : ViewModel() {

    private val apiService: ApiService = Retrofit.Builder()
        .baseUrl("https://10.0.2.2:5001/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    fun sendMessage(
        token: String,
        fromUsername: String,
        toQrCode: String,
        message: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = SendMessageRequest(fromUsername, toQrCode, message)
                val response = apiService.sendMessage("Bearer $token", request)
                onResult(response.isSuccessful)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
        }
    }
}
