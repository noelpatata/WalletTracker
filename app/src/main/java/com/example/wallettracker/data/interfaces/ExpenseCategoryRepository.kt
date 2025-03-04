package com.example.wallettracker.data.interfaces

import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategory

interface ExpenseCategoryRepository {
    fun getExpenseCategories(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun getExpenseCategoryById(
        catId: Long,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun createExpenseCategories(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun deleteById(
        catId: Long,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun editName(
        category: ExpenseCategory,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )
}