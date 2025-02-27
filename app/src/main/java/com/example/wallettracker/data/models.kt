package com.example.wallettracker.data

data class DataResponse(
    val signature: String,
    val symmetric_keys: SymmetricResponse
)
data class SymmetricResponse(
    val encrypted_aes_key: String,
    val iv: String,
    val ciphertext: String,
    val tag: String
)
data class SuccessResponse(
    val success: Boolean,
    val message: String
)
