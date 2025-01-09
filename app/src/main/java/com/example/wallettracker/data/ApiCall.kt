package com.example.wallettracker.data

import com.example.wallettracker.data.expenseCategory.ExpenseCategoryEPs
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiCall {
    private const val BASE_URL = "http://192.168.1.137:8081"

    val expenseCategory: ExpenseCategoryEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseCategoryEPs::class.java)
    }

    val expense: ExpenseCategoryEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseCategoryEPs::class.java)
    }
}