package com.example.wallettracker.data.expense

import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRequest
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpenseEPs {


    @DELETE("Expense/all/")
    fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
    ): Call<SuccessResponse>

    @GET("Expense/")
    fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Query("catId") catId: Long
    ): Call<DataResponse>

    @POST("Expense/edit/")
    fun edit(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: ExpenseRequest
    ): Call<SuccessResponse>

    @POST("Expense/create/")
    fun createExpense(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expense: ExpenseRequest
    ): Call<DataResponse>

    @GET("Expense/Id/")
    fun getById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Query("expenseId") expenseId: Long
    ): Call<DataResponse>

    @DELETE("Expense/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Query("expenseId") expenseId: Long
    ): Call<SuccessResponse>
}