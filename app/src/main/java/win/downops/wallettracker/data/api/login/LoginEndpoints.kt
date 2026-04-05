package win.downops.wallettracker.data.api.login

import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.api.communication.responses.LoginResponse
import win.downops.wallettracker.data.api.communication.responses.ServerPubKeyResponse

interface LoginEndpoints {

    @POST("api/v1/register/")
    suspend fun register(
        @Body login: LoginRequest
    ): Response<BaseResponse<ServerPubKeyResponse>>

    @POST("api/v1/login/")
    suspend fun login(
        @Body login: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    @POST("api/v1/refresh/")
    fun refreshSync(
        @Header("Authorization") token: String,
    ): Call<BaseResponse<LoginResponse>>

    @POST("api/v1/refresh/")
    suspend fun refresh(
        @Header("Authorization") token: String,
    ): Response<BaseResponse<LoginResponse>>

    @POST("api/v1/logout/")
    suspend fun logout(
        @Header("Authorization") token: String,
    ): Response<BaseResponse<Nothing>>

    @GET("api/v1/getUserServerPubKey/")
    suspend fun getUserServerPubKey(
        @Header("Authorization") token: String,
    ): Response<BaseResponse<ServerPubKeyResponse>>

    @POST("api/v1/setUserClientPubKey/")
    suspend fun setUserClientPubKey(
        @Header("Authorization") token: String,
        @Body payload: ServerPubKeyRequest
    ): Response<BaseResponse<Nothing>>
}
