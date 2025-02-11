package com.example.wallettracker.data

import com.example.wallettracker.data.expense.ExpenseEPs
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryEPs
import com.example.wallettracker.data.login.LoginEPs
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiCall {
    private const val BASE_URL = "http://172.20.0.190:8080"

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(RSACipherInterceptor()) // Add the encryption interceptor
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val login: LoginEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginEPs::class.java)
    }

    val expenseCategory: ExpenseCategoryEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseCategoryEPs::class.java)
    }

    val expense: ExpenseEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseEPs::class.java)
    }


}