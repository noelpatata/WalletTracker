package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.CipheredRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface OnlineExpenseCategoryEPs {

    @GET("/api/v1/ExpenseCategory/")
    fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

    @GET("/api/v1/ExpenseCategory/all")
    fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String
    ): Call<BaseResponse<CipheredResponse>>

    @POST("/api/v1/ExpenseCategory/")
    fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

    @PATCH("/api/v1/ExpenseCategory/")
    fun editName(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

    @DELETE("/api/v1/ExpenseCategory/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<BaseResponse<CipheredResponse>>

}