package com.example.wallettracker.data.expense

import com.example.wallettracker.data.communication.SuccessResponse

interface ExpenseRepository {
    fun getById(
        expenseId: Long,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    )

    fun getByCatId(
        catId: Long,
        onSuccess: (List<Expense>) -> Unit,
        onFailure: (String) -> Unit
    )

    fun create(
        expense: Expense,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    )

    fun edit(
        expense: Expense,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    )

    fun deleteById(
        expenseId: Long,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (String) -> Unit
    )

    fun deleteAll(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (String) -> Unit
    )
}
