package com.autoqr.model

data class SendMessageRequest(
    val fromUsername: String,
    val toQrCode: String,
    val body: String
)
