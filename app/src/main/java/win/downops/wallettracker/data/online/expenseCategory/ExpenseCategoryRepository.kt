package win.downops.wallettracker.data.online.expenseCategory

import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory

interface ExpenseCategoryRepository {
    suspend fun getAll(): AppResult<List<ExpenseCategory>>
    suspend fun getById(catId: Long): AppResult<ExpenseCategory?>
    suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?>
    suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?>
    suspend fun deleteById(catId: Long): AppResult<Unit>
}
