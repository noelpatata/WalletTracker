package com.example.wallettracker.data.expenseCategory
import com.example.wallettracker.util.Messages.authenticationErrorMessage
import com.example.wallettracker.util.Messages.noDataMessage
import BaseDAO
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.ExpenseCategoryIdRequest
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.util.Messages.invalidData
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseCategoryDAO(context: Context) : BaseDAO<ExpenseCategory>(context),
    ExpenseCategoryRepository {

    override fun getAll(onSuccess: (List<ExpenseCategory>) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        ApiCall.expenseCategory.getExpenseCategories("Bearer $token", cipheredText)
            .enqueue(handleListResponse(onSuccess, onFailure))
    }

    override fun getById(catId: Long, onSuccess: (ExpenseCategory?) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryIdRequest(catId))?.let { cipheredData ->
            ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun create(category: ExpenseCategory, onSuccess: (ExpenseCategory?) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryRequest(category))?.let { cipheredData ->
            ApiCall.expenseCategory.createExpenseCategories("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun deleteById(catId: Long, onSuccess: () -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryIdRequest(catId))?.let { cipheredData ->
            ApiCall.expenseCategory.deleteById("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleEmptyResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun edit(category: ExpenseCategory, onSuccess: (ExpenseCategory?) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryRequest(category))?.let { cipheredData ->
            ApiCall.expenseCategory.editName("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    private inline fun <reified T> handleResponse(
        crossinline onSuccess: (T?) -> Unit,
        noinline onFailure: (SuccessResponse) -> Unit
    ): Callback<BaseResponse<CipheredResponse>> {
        return object : Callback<BaseResponse<CipheredResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<CipheredResponse>>,
                response: Response<BaseResponse<CipheredResponse>>
            ) {
                response.body()?.let { body ->

                    val jsonData = validateCipheredResponse(body, onFailure) ?: return

                    deserialize<BaseResponse<T>>(jsonData)?.let { deserializedResponse ->

                        if (deserializedResponse.success) {
                            onSuccess(deserializedResponse.data)
                        } else {
                            onFailure(SuccessResponse(false, deserializedResponse.message))
                        }

                    } ?: onFailure(SuccessResponse(false, invalidData))
                } ?: onFailure(SuccessResponse(false, noDataMessage))
            }

            override fun onFailure(call: Call<BaseResponse<CipheredResponse>>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }

    private fun handleEmptyResponse(
        onSuccess: () -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ): Callback<BaseResponse<CipheredResponse>> {
        return object : Callback<BaseResponse<CipheredResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<CipheredResponse>>,
                response: Response<BaseResponse<CipheredResponse>>
            ) {
                response.body()?.let { body ->

                    val jsonData = validateCipheredResponse(body, onFailure) ?: return

                    deserialize<BaseResponse<Nothing>>(jsonData)?.let { deserializedResponse ->
                        if (deserializedResponse.success) {
                            onSuccess()
                        } else {
                            onFailure(SuccessResponse(false, deserializedResponse.message))
                        }
                    } ?: onFailure(SuccessResponse(false, invalidData))

                } ?: onFailure(SuccessResponse(false, noDataMessage))
            }

            override fun onFailure(call: Call<BaseResponse<CipheredResponse>>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }

    private inline fun <reified T> handleListResponse(
        crossinline onSuccess: (List<T>) -> Unit,
        noinline onFailure: (SuccessResponse) -> Unit
    ): Callback<BaseResponse<CipheredResponse>> {
        return object : Callback<BaseResponse<CipheredResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<CipheredResponse>>,
                response: Response<BaseResponse<CipheredResponse>>
            ) {
                response.body()?.let { body ->

                    val jsonData = validateCipheredResponse(body, onFailure) ?: return

                    deserialize<BaseResponse<List<T>>>(jsonData)?.let { deserializedResponse ->

                        if (deserializedResponse.success) {
                            deserializedResponse.data?.let(onSuccess)
                        } else {
                            onFailure(SuccessResponse(false, deserializedResponse.message))
                        }

                    } ?: onFailure(SuccessResponse(false, invalidData))
                } ?: onFailure(SuccessResponse(false, noDataMessage))
            }

            override fun onFailure(call: Call<BaseResponse<CipheredResponse>>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }


    private inline fun <reified T> deserialize(jsonData: String): T? {
        val type = object : com.google.gson.reflect.TypeToken<BaseResponse<T?>>() {}.type
        val baseResponse: BaseResponse<T?> = GsonBuilder().create().fromJson(jsonData, type)
        return baseResponse.data
    }
}
