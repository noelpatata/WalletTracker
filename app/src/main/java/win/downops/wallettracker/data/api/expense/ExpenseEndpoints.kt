package win.downops.wallettracker.data.api.expense

import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse

interface ExpenseEndpoints {

    @POST("/api/v1/Expense/id")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expenseId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/Expense/category/")
    suspend fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body catId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/Expense/")
    suspend fun create(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expense: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @PATCH("/api/v1/Expense/")
    suspend fun edit(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/Expense/delete")
    suspend fun deleteById(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expenseId : CipheredRequest
    ): Response<BaseResponse<Unit>>

    @DELETE("/api/v1/Expense/all/")
    suspend fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
    ): Response<BaseResponse<Unit>>

}