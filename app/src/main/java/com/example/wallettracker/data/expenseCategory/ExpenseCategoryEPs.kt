package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.SuccessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpenseCategoryEPs {


    @GET("ExpenseCategory/")
    fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Query("ciphered") ciphered: String
    ): Call<List<ExpenseCategoryResponse>>

    @GET("ExpenseCategory/Id/")
    fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Query("catId") catId: Long
    ): Call<ExpenseCategoryResponse>

    @POST("ExpenseCategory/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Body expenseCategory: ExpenseCategoryRequest
    ): Call<ExpenseCategoryResponse>

    @DELETE("ExpenseCategory/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Query("catId") catId: Long
    ): Call<SuccessResponse>

    @POST("ExpenseCategory/editName")
    fun editName(
        @Header("Authorization") token: String,
        @Body expenseCategory: ExpenseCategoryRequest
    ): Call<SuccessResponse>
}