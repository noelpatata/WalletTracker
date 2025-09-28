package com.example.wallettracker.data.communication


data class CipheredRequest(
    val encrypted_aes_key: String,
    val iv: String,
    val ciphertext: String,
    val tag: String
)
data class ExpenseCategoryIdRequest(
    val catId: Long
)
data class ExpenseIdRequest(
    val expenseId: Long
)

class ExpenseCategoryRequest(
    val id: Long,
    val name: String
)
