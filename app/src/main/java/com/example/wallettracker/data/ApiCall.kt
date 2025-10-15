package com.example.wallettracker.data

import com.example.wallettracker.data.expense.OnlineExpenseEPs
import com.example.wallettracker.data.expenseCategory.OnlineExpenseCategoryEPs
import com.example.wallettracker.data.login.LoginEPs
import okhttp3.ConnectionPool
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiCall {
    private const val BASE_URL = "http://192.168.1.141:5000/"

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

    val login: LoginEPs by lazy { retrofit.create(LoginEPs::class.java) }
    val expenseCategory: OnlineExpenseCategoryEPs by lazy { retrofit.create(OnlineExpenseCategoryEPs::class.java) }
    val expense: OnlineExpenseEPs by lazy { retrofit.create(OnlineExpenseEPs::class.java) }
}

