package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.CipheredRequest
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
    ): Call<BaseResponse<CipheredResponse>>

    @POST("ExpenseCategory/Id/")
    fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

    @POST("ExpenseCategory/create/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

    @POST("ExpenseCategory/delete/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<SuccessResponse>

    @POST("ExpenseCategory/editName/")
    fun editName(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<SuccessResponse>
}