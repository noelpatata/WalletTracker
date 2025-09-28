package com.example.wallettracker.data.expense

import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.communication.CipheredRequest
import com.example.wallettracker.data.communication.CipheredResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST

interface OnlineExpenseEPs {

    @GET("/api/v1/Expense")
    fun getById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId: CipheredRequest
    ): Call<CipheredResponse>

    @GET("/api/v1/Expense/category/")
    fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<CipheredResponse>

    @POST("/api/v1/Expense/")
    fun create(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expense: CipheredRequest
    ): Call<CipheredResponse>

    @PATCH("/api/v1/Expense/")
    fun edit(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<CipheredResponse>

    @DELETE("/api/v1/Expense/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId : CipheredRequest
    ): Call<SuccessResponse>

    @DELETE("/api/v1/Expense/all/")
    fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
    ): Call<SuccessResponse>

}