package com.example.wallettracker.data.login

import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.SuccessResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginEPs {

    @POST("api/v1/register/")
    fun register(
        @Body login: LoginRequest
    ): Call<ServerPubKeyResponse>

    @POST("api/v1/login/")
    fun login(
        @Body login: LoginRequest
    ): Call<BaseResponse<LoginResponse>>

    @POST("api/v1/autologin/")
    fun autologin(
        @Body payload: AutoLoginRequest
    ): Call<LoginResponse>

    @POST("api/v1/getUserServerPubKey/")
    fun getUserServerPubKey(
        @Body payload: LoginRequest
    ): Call<BaseResponse<ServerPubKeyResponse>>

    @POST("api/v1/setUserClientPubKey/")
    fun setUserClientPubKey(
        @Body payload: ServerPubKeyRequest
    ): Call<BaseResponse<Nothing>>
}