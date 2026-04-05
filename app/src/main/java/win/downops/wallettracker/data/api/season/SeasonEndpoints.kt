package win.downops.wallettracker.data.api.season

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse

interface SeasonEndpoints {

    @GET("api/v${BuildConfig.API_VERSION}/Season/all")
    suspend fun getAll(): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Season/id")
    suspend fun getById(
        @Body body: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Season/")
    suspend fun getOrCreate(
        @Body body: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Season/delete")
    suspend fun deleteById(
        @Body body: CipheredRequest
    ): Response<BaseResponse<Unit>>
}
