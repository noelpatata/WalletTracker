package win.downops.wallettracker.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.api.AuthInterceptor
import win.downops.wallettracker.data.api.TokenAuthenticator
import win.downops.wallettracker.data.api.expense.ExpenseEndpoints
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryEndpoints
import win.downops.wallettracker.data.api.importe.ImporteEndpoints
import win.downops.wallettracker.data.api.login.LoginEndpoints
import win.downops.wallettracker.data.api.season.SeasonEndpoints
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideLoginEndpoints(retrofit: Retrofit): LoginEndpoints =
        retrofit.create(LoginEndpoints::class.java)

    @Provides
    @Singleton
    fun provideExpenseEndpoints(retrofit: Retrofit): ExpenseEndpoints =
        retrofit.create(ExpenseEndpoints::class.java)

    @Provides
    @Singleton
    fun provideExpenseCategoryEndpoints(retrofit: Retrofit): ExpenseCategoryEndpoints =
        retrofit.create(ExpenseCategoryEndpoints::class.java)

    @Provides
    @Singleton
    fun provideSeasonEndpoints(retrofit: Retrofit): SeasonEndpoints =
        retrofit.create(SeasonEndpoints::class.java)

    @Provides
    @Singleton
    fun provideImporteEndpoints(retrofit: Retrofit): ImporteEndpoints =
        retrofit.create(ImporteEndpoints::class.java)
}
