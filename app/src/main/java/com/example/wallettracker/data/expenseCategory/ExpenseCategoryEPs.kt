package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.DataResponse
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
        @Header("Cipher") cipher: String
    ): Call<DataResponse>

    @POST("ExpenseCategory/Id/")
    fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CatIdRequest
    ): Call<DataResponse>

    @POST("ExpenseCategory/create/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: ExpenseCategoryRequest
    ): Call<DataResponse>

    @POST("ExpenseCategory/delete/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CatIdRequest
    ): Call<SuccessResponse>

    @POST("ExpenseCategory/editName/")
    fun editName(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: ExpenseCategoryRequest
    ): Call<SuccessResponse>
}