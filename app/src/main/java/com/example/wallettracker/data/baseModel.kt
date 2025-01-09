package com.example.wallettracker.data

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)
data class ErrorResponse(
    val message: String
)

