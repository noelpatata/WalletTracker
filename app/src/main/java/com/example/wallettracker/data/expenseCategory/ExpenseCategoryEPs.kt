package com.example.wallettracker.data.expenseCategory

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpenseCategoryEPs {


    @GET("ExpenseCategory/")
    fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int
    ): Call<List<ExpenseCategoryResponse>>

    @POST("ExpenseCategory/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int,
        @Body expenseCategory: ExpenseCategoryRequest
    ): Call<ExpenseCategoryResponse>
}