package com.example.wallettracker.data.expense

import com.example.wallettracker.data.login.AppResult

interface ExpenseRepository {
    suspend fun getById(expenseId: Long): AppResult<Expense>
    suspend fun getByCatId(catId: Long): AppResult<List<Expense>>
    suspend fun create(expense: Expense): AppResult<Expense>
    suspend fun edit(expense: Expense): AppResult<Expense>
    suspend fun deleteById(expenseId: Long): AppResult<Unit>
    suspend fun deleteAll(): AppResult<Unit>
}