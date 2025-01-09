package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.LoginRequest
import com.example.wallettracker.data.LoginResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Body

interface ExpenseCategoryAPI {
    @GET("login")
    fun login(
        @Header("Authorization") authHeader: String
    ): Call<LoginResponse>

    @GET("ExpenseCategory/")
    fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int
    ): Call<List<ExpenseCategory>>
}