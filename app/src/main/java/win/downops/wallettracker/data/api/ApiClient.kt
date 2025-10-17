package win.downops.wallettracker.data.api

import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.api.expense.ExpenseEndpoints
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryEndpoints
import win.downops.wallettracker.data.api.login.LoginEndpoints
import okhttp3.ConnectionPool
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = BuildConfig.API_BASE_URL

    private val okHttpClient by lazy {

        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .build()
    }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun isServerReachable(): Boolean {
        return try {
            val request = Request.Builder()
                .url(BASE_URL)
                .head()
                .build()
            val response = okHttpClient.newCall(request).execute()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    val login: LoginEndpoints by lazy { retrofit.create(LoginEndpoints::class.java) }
    val expenseCategory: ExpenseCategoryEndpoints by lazy { retrofit.create(ExpenseCategoryEndpoints::class.java) }
    val expense: ExpenseEndpoints by lazy { retrofit.create(ExpenseEndpoints::class.java) }
}

