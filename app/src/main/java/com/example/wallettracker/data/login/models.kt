package com.example.wallettracker.data.login

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)
data class AutoLoginRequest(
    val userId: Int,
    val ciphered: String
)
data class ServerPubKeyRequest(
    val publicKey: String
)
data class ServerPubKeyResponse(
    val userId: Int,
    val publicKey: String
)
