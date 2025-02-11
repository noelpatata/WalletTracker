package com.example.wallettracker.data.expense

data class ExpenseResponse(
    val category: Long,
    val expenseDate: String,
    val id: Long,
    val price: Double,
    val user: Int
)