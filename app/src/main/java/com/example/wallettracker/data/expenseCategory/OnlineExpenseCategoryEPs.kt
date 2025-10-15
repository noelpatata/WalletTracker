package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.CipheredRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface OnlineExpenseCategoryEPs {

    @POST("/api/v1/ExpenseCategory/id")
    suspend fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @GET("/api/v1/ExpenseCategory/all")
    suspend fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/ExpenseCategory/")
    suspend fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @PATCH("/api/v1/ExpenseCategory/")
    suspend fun editName(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @DELETE("/api/v1/ExpenseCategory/")
    suspend fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

}