package com.example.wallettracker.data.login

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val userId: Int,
    val token: String
)