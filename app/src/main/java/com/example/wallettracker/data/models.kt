package com.example.wallettracker.data

open class BaseDAO(val token: String, val userId: Int) {
}

data class ErrorResponse(
    val message: String
)

