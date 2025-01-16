package com.example.wallettracker.data.expense

import com.example.wallettracker.data.SuccessResponse
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
        @Query("userId") userId: Int
    ): Call<SuccessResponse>

    @GET("Expense/")
    fun getByCatId(
        @Header("Authorization") token: String,
        @Query("userId") userId: Int,
        @Query("catId") catId: Long
    ): Call<List<ExpenseResponse>>
}