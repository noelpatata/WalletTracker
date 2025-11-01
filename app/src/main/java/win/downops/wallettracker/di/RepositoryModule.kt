package win.downops.wallettracker.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.LoginRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.login.LoginHttpService
import win.downops.wallettracker.data.sqlite.session.SessionSqlService
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLoginRepository(
        impl: LoginHttpService
    ): LoginRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionSqlService
    ): SessionRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(
        impl: ExpenseRepositoryProvider
    ): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindExpenseCategoryRepository(
        impl: ExpenseCategoryRepositoryProvider
    ): ExpenseCategoryRepository
}