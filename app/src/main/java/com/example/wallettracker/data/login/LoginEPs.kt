package com.example.wallettracker.data.login

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface LoginEPs {
    @GET("login")
    fun login(
        @Header("Authorization") authHeader: String
    ): Call<LoginResponse>

    @GET("autologin")
    fun autologin(
        @Query("userId") userId: Int
    ): Call<LoginResponse>
}