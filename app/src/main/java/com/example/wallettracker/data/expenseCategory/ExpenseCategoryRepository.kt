package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.login.AppResult

interface ExpenseCategoryRepository {
    suspend fun getAll(): AppResult<List<ExpenseCategory>>
    suspend fun getById(catId: Long): AppResult<ExpenseCategory?>
    suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?>
    suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?>
    suspend fun deleteById(catId: Long): AppResult<Unit>
}
