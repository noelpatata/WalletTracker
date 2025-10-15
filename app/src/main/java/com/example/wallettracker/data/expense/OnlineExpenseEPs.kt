package com.example.wallettracker.data.expense

import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.communication.CipheredRequest
import com.example.wallettracker.data.communication.CipheredResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface OnlineExpenseEPs {

    @GET("/api/v1/Expense")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId: CipheredRequest
    ): Response<CipheredResponse>

    @GET("/api/v1/Expense/category/")
    suspend fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Response<CipheredResponse>

    @POST("/api/v1/Expense/")
    suspend fun create(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expense: CipheredRequest
    ): Response<CipheredResponse>

    @PATCH("/api/v1/Expense/")
    suspend fun edit(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<CipheredResponse>

    @DELETE("/api/v1/Expense/")
    suspend fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId : CipheredRequest
    ): Response<SuccessResponse>

    @DELETE("/api/v1/Expense/all/")
    suspend fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
    ): Response<SuccessResponse>

}