package com.example.wallettracker.data.communication

data class CipheredResponse(
    val signature: String,
    val encrypted_data: CipheredRequest
)

data class BaseResponse<T>(
    val data: T?,
    val message: String,
    val success: Boolean
)
data class SuccessResponse(
    val success: Boolean,
    val message: String
)