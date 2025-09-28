package com.example.wallettracker.data.login

import com.example.wallettracker.data.communication.SuccessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

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