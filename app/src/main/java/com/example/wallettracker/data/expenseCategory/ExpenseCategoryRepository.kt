package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.communication.SuccessResponse

interface ExpenseCategoryRepository {
    fun getAll(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun getById(
        catId: Long,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun create(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun deleteById(
        catId: Long,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )

    fun edit(
        category: ExpenseCategory,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    )
}