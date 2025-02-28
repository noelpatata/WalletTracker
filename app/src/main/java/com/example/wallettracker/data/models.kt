package com.example.wallettracker.data

data class DataResponse(
    val signature: String,
    val encrypted_data: SymmetricResponse
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
data class CatIdRequest(val catId: Long)
data class ExpenseIdRequest(val expenseId: Long)
