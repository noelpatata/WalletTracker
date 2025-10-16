package win.downops.wallettracker.data.login

import win.downops.wallettracker.data.communication.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface LoginEPs {

    @POST("api/v1/register/")
    suspend fun register(
        @Body login: LoginRequest
    ): Response<BaseResponse<ServerPubKeyResponse>>

    @POST("api/v1/login/")
    suspend fun login(
        @Body login: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

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
