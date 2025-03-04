package com.example.wallettracker.data.interfaces

import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expense.Expense

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

    fun createExpense(
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
