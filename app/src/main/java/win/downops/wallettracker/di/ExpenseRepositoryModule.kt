package win.downops.wallettracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.*
import win.downops.wallettracker.data.api.ApiClient.isServerReachable
import win.downops.wallettracker.data.api.expense.ExpenseHttpService
import win.downops.wallettracker.data.sqlite.expense.ExpenseSqlService

@Module
@InstallIn(SingletonComponent::class)
class ExpenseRepositoryModule {

    @Provides
    fun provideRepository(
        apiRepository: ExpenseHttpService,
        sqliteRepository: ExpenseSqlService
    ): ExpenseRepository {
        return if (isServerReachable()) {
            apiRepository
        } else {
            sqliteRepository
        }
    }
}