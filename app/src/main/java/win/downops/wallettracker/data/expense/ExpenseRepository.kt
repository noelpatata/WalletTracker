package win.downops.wallettracker.data.expense

import win.downops.wallettracker.data.login.AppResult

interface ExpenseRepository {
    suspend fun getByCatId(catId: Long): AppResult<List<Expense>>
    suspend fun getById(expenseId: Long): AppResult<Expense?>
    suspend fun create(expense: Expense): AppResult<Expense?>
    suspend fun edit(expense: Expense): AppResult<Expense?>
    suspend fun deleteById(expenseId: Long): AppResult<Unit>
    suspend fun deleteAll(): AppResult<Unit>
}