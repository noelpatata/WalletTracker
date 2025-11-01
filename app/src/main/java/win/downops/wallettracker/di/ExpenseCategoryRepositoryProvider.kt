package win.downops.wallettracker.di

import win.downops.wallettracker.data.ExpenseCategoryRepository
import javax.inject.Inject
import javax.inject.Singleton
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryHttpService
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.data.sqlite.expenseCategory.ExpenseCategorySqlService

@Singleton
class ExpenseCategoryRepositoryProvider @Inject constructor(
    private val apiRepository: dagger.Lazy<ExpenseCategoryHttpService>,
    private val localRepository: dagger.Lazy<ExpenseCategorySqlService>
) : ExpenseCategoryRepository {

    private suspend fun repo(): ExpenseCategoryRepository {
        return if (ApiClient.isServerReachable())
            apiRepository.get()
        else
            localRepository.get()

    }

    override suspend fun getAll(): AppResult<List<ExpenseCategory>> {
        return repo().getAll()
    }

    override suspend fun getById(catId: Long): AppResult<ExpenseCategory?> {
        return repo().getById(catId)
    }

    override suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return repo().create(category)
    }

    override suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return repo().edit(category)
    }

    override suspend fun deleteById(catId: Long): AppResult<Unit> {
        return repo().deleteById(catId)
    }
}

