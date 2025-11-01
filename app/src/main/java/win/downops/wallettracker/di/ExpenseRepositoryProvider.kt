package win.downops.wallettracker.di

import win.downops.wallettracker.data.ExpenseRepository
import javax.inject.Inject
import javax.inject.Singleton
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.expense.ExpenseHttpService
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.sqlite.expense.ExpenseSqlService

@Singleton
class ExpenseRepositoryProvider @Inject constructor(
    private val apiRepository: dagger.Lazy<ExpenseHttpService>,
    private val localRepository: dagger.Lazy<ExpenseSqlService>
) : ExpenseRepository {
    private suspend fun repo(): ExpenseRepository {
        return if (ApiClient.isServerReachable())
            apiRepository.get()
        else
            localRepository.get()
    }

    override suspend fun getByCatId(catId: Long): AppResult<List<Expense>> {
        return repo().getByCatId(catId)
    }

    override suspend fun getById(expenseId: Long): AppResult<Expense?> {
        return repo().getById(expenseId)
    }

    override suspend fun create(expense: Expense): AppResult<Expense?> {
        return repo().create(expense)
    }

    override suspend fun edit(expense: Expense): AppResult<Expense?> {
        return repo().edit(expense)
    }

    override suspend fun deleteById(expenseId: Long): AppResult<Unit> {
        return repo().deleteById(expenseId)
    }

    override suspend fun deleteAll(): AppResult<Unit> {
        return repo().deleteAll()
    }
}

