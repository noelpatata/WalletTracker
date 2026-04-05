package win.downops.wallettracker.data.api.expense

import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PATCH
import retrofit2.http.POST
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse

interface ExpenseEndpoints {

    @POST("api/v${BuildConfig.API_VERSION}/Expense/id")
    suspend fun getById(
        @Body expenseId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Expense/category/")
    suspend fun getByCatId(
        @Body catId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Expense/season/")
    suspend fun getBySeasonId(
        @Body seasonId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Expense/")
    suspend fun create(
        @Body expense: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @PATCH("api/v${BuildConfig.API_VERSION}/Expense/")
    suspend fun edit(
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("api/v${BuildConfig.API_VERSION}/Expense/delete")
    suspend fun deleteById(
        @Body expenseId : CipheredRequest
    ): Response<BaseResponse<Unit>>

    @DELETE("api/v${BuildConfig.API_VERSION}/Expense/all/")
    suspend fun deleteAll(): Response<BaseResponse<Unit>>

}
