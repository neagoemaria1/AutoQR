package com.autoqr.model

data class UserProfileResponse(
    val email: String,
    val username: String,
    val qrCode: String,
    val profileImageUrl: String,
    val deviceToken: String?
)
