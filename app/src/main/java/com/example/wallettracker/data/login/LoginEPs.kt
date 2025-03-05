package com.example.wallettracker.data.login

import com.example.wallettracker.data.SuccessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginEPs {

    @POST("register/")
    fun register(
        @Body login: LoginRequest
    ): Call<ServerPubKeyResponse>

    @POST("login/")
    fun login(
        @Body login: LoginRequest
    ): Call<LoginResponse>

    @POST("autologin/")
    fun autologin(
        @Body payload: AutoLoginRequest
    ): Call<LoginResponse>

    @POST("getUserServerPubKey/")
    fun getUserServerPubKey(
        @Body payload: LoginRequest
    ): Call<ServerPubKeyResponse>

    @POST("setUserClientPubKey/")
    fun setUserClientPubKey(
        @Body payload: ServerPubKeyRequest
    ): Call<SuccessResponse>
}