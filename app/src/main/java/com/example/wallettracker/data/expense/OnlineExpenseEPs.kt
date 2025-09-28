package com.example.wallettracker.data.expense

import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.communication.CipheredRequest
import com.example.wallettracker.data.communication.CipheredResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.POST

interface OnlineExpenseEPs {


    @DELETE("Expense/all/")
    fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
    ): Call<SuccessResponse>

    @POST("Expense/CatId/")
    fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: CipheredRequest
    ): Call<CipheredResponse>

    @POST("Expense/edit/")
    fun edit(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Call<CipheredResponse>

    @POST("Expense/create/")
    fun create(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expense: CipheredRequest
    ): Call<CipheredResponse>

    @POST("Expense/Id")
    fun getById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId: CipheredRequest
    ): Call<CipheredResponse>

    @POST("Expense/delete/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId : CipheredRequest
    ): Call<SuccessResponse>
}