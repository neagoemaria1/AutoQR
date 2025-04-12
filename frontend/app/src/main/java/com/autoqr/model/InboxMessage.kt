package com.autoqr.model

data class InboxMessage(
    val fromUsername: String,
    val toUsername: String,
    val message: String,
    val timestamp: Long,
    val messageType: String = "alert",
    val isRead: Boolean = false,
    val replyTo: String? = null
)
