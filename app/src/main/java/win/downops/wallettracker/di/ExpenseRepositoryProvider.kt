package win.downops.wallettracker.di

import win.downops.wallettracker.data.ExpenseRepository
import javax.inject.Inject
import javax.inject.Singleton
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.expense.ExpenseHttpService
import win.downops.wallettracker.data.sqlite.expense.ExpenseSqlService

@Singleton
class ExpenseRepositoryProvider @Inject constructor(
    private val apiRepositoryProvider: dagger.Lazy<ExpenseHttpService>,
    private val sqliteRepositoryProvider: dagger.Lazy<ExpenseSqlService>
) {
    suspend fun get(): ExpenseRepository {
        return if (ApiClient.isServerReachable()) {
            apiRepositoryProvider.get()
        } else {
            sqliteRepositoryProvider.get()
        }
    }
}

