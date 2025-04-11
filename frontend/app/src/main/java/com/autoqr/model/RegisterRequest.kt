package com.autoqr.model

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String
)