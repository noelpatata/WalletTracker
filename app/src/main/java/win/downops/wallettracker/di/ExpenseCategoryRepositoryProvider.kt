package win.downops.wallettracker.di

import win.downops.wallettracker.data.ExpenseCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryHttpService
import win.downops.wallettracker.data.sqlite.expenseCategory.ExpenseCategorySqlService

@Singleton
class ExpenseCategoryRepositoryProvider @Inject constructor(
    private val apiRepositoryProvider: dagger.Lazy<ExpenseCategoryHttpService>,
    private val sqliteRepositoryProvider: dagger.Lazy<ExpenseCategorySqlService>
) {
    suspend fun get(): ExpenseCategoryRepository {
        return if (ApiClient.isServerReachable()) {
            apiRepositoryProvider.get()
        } else {
            sqliteRepositoryProvider.get()
        }
    }
}

