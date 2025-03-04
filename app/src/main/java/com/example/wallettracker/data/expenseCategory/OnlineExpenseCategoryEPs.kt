package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SymmetricResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface OnlineExpenseCategoryEPs {


    @GET("ExpenseCategory/")
    fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String
    ): Call<DataResponse>

    @POST("ExpenseCategory/Id/")
    fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: SymmetricResponse
    ): Call<DataResponse>

    @POST("ExpenseCategory/create/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: SymmetricResponse
    ): Call<DataResponse>

    @POST("ExpenseCategory/delete/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: SymmetricResponse
    ): Call<SuccessResponse>

    @POST("ExpenseCategory/editName/")
    fun editName(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: SymmetricResponse
    ): Call<SuccessResponse>
}