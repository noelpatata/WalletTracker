package com.example.wallettracker.data

import com.example.wallettracker.data.expenseCategory.ExpenseCategoryAPI
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiCall {
    private const val BASE_URL = "http://192.168.33.96:8081/"

    val api: ExpenseCategoryAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ExpenseCategoryAPI::class.java)
    }
}