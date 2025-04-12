package com.autoqr.services

import android.content.Context
import com.autoqr.model.PredefinedMessagesResponse
import com.autoqr.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PredefinedMessagesRepository(private val context: Context) {
    suspend fun loadPredefinedMessages(): PredefinedMessagesResponse? = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.create(context).getPredefinedMessages()
            if (response.isSuccessful) {
                return@withContext response.body()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext null
    }
}
