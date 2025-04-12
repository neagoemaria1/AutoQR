package com.autoqr.services

import android.content.Context
import com.autoqr.model.InboxMessage
import com.autoqr.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InboxRepository(private val context: Context) {
    suspend fun fetchInboxMessages(token: String): List<InboxMessage> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.create(context).getInboxMessages("Bearer $token")
            if (response.isSuccessful) {
                return@withContext response.body()?.mapNotNull { item ->
                    try {
                        val timestampMap = item["timestamp"] as? Map<*, *>
                        val seconds = (timestampMap?.get("seconds") as? Number)?.toLong() ?: 0L

                        InboxMessage(
                            fromUsername = item["fromUsername"]?.toString().orEmpty(),
                            toUsername = item["toUsername"]?.toString().orEmpty(),
                            message = item["message"]?.toString().orEmpty(),
                            timestamp = seconds,
                            messageType = item["messageType"]?.toString() ?: "alert",
                            isRead = item["isRead"] as? Boolean ?: false
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                } ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}
