package com.example.wallettracker.data

open class BaseDAO(val token: String, val userId: Int) {
}

data class ErrorResponse(
    val error: String
)
data class SuccessResponse(
    val success: Boolean,
    val message: String
)

