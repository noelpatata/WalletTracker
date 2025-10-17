package win.downops.wallettracker.data.api.expenseCategory

import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest

interface ExpenseCategoryEndpoints {

    @POST("/api/v1/ExpenseCategory/id")
    suspend fun getExpenseCategoryById(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body catId: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @GET("/api/v1/ExpenseCategory/all")
    suspend fun getExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/ExpenseCategory/")
    suspend fun createExpenseCategories(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @PATCH("/api/v1/ExpenseCategory/")
    suspend fun editName(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body expenseCategory: CipheredRequest
    ): Response<BaseResponse<CipheredResponse>>

    @POST("/api/v1/ExpenseCategory/delete")
    suspend fun deleteById(
        @Header("Authorization") token: String,
        @Header("Signature") cipher: String,
        @Body catId: CipheredRequest
    ): Response<BaseResponse<Unit>>

}