package win.downops.wallettracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.api.ApiClient.isServerReachable
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryHttpService
import win.downops.wallettracker.data.sqlite.expenseCategory.ExpenseCategorySqlService

@Module
@InstallIn(SingletonComponent::class)
class ExpenseCategoryRepositoryModule {

    @Provides
    fun provideRepository(
        apiRepository: ExpenseCategoryHttpService,
        sqliteRepository: ExpenseCategorySqlService
    ): ExpenseCategoryRepository {
        return if (isServerReachable()) {
            apiRepository
        } else {
            sqliteRepository
        }
    }
}