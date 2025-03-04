package com.example.wallettracker.data.expenseCategory
import com.example.wallettracker.util.Constantes.authenticationErrorMessage
import com.example.wallettracker.util.Constantes.noDataMessage
import BaseDAO
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.interfaces.ExpenseCategoryRepository
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseCategoryDAO(context: Context) : BaseDAO<ExpenseCategory>(context),
    ExpenseCategoryRepository {

    override fun getExpenseCategories(onSuccess: (List<ExpenseCategory>) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        ApiCall.expenseCategory.getExpenseCategories("Bearer $token", cipheredText)
            .enqueue(handleListResponse(onSuccess, onFailure))
    }

    override fun getExpenseCategoryById(catId: Long, onSuccess: (ExpenseCategory) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(CatIdRequest(catId))?.let { cipheredData ->
            ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun createExpenseCategories(category: ExpenseCategory, onSuccess: (ExpenseCategory) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryRequest(category))?.let { cipheredData ->
            ApiCall.expenseCategory.createExpenseCategories("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun deleteById(catId: Long, onSuccess: (SuccessResponse) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(CatIdRequest(catId))?.let { cipheredData ->
            ApiCall.expenseCategory.deleteById("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleSuccessResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    override fun editName(category: ExpenseCategory, onSuccess: (SuccessResponse) -> Unit, onFailure: (SuccessResponse) -> Unit) {
        encryptData(ExpenseCategoryRequest(category))?.let { cipheredData ->
            ApiCall.expenseCategory.editName("Bearer $token", cipheredText, cipheredData)
                .enqueue(handleSuccessResponse(onSuccess, onFailure))
        } ?: onFailure(SuccessResponse(false, authenticationErrorMessage))
    }

    private inline fun <reified T> handleResponse(crossinline onSuccess: (T) -> Unit, crossinline onFailure: (SuccessResponse) -> Unit): Callback<DataResponse> {
        return object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val jsonData = verifyData(it)
                        if (jsonData.isNotEmpty()) singlemap<T>(jsonData)?.let(onSuccess)
                        else onFailure(SuccessResponse(false, authenticationErrorMessage))
                    } ?: onFailure(SuccessResponse(false, noDataMessage))
                } else {
                    onFailure(SuccessResponse(false, response.message()))
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }

    private inline fun <reified T> handleListResponse(crossinline onSuccess: (List<T>) -> Unit, crossinline onFailure: (SuccessResponse) -> Unit): Callback<DataResponse> {
        return object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val jsonData = verifyData(it)
                        if (jsonData.isNotEmpty()) onSuccess(map(jsonData))
                        else onFailure(SuccessResponse(false, noDataMessage))
                    } ?: onFailure(SuccessResponse(false, noDataMessage))
                } else {
                    onFailure(SuccessResponse(false, response.message()))
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }

    private fun handleSuccessResponse(onSuccess: (SuccessResponse) -> Unit, onFailure: (SuccessResponse) -> Unit): Callback<SuccessResponse> {
        return object : Callback<SuccessResponse> {
            override fun onResponse(call: Call<SuccessResponse>, response: Response<SuccessResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: onFailure(SuccessResponse(false, response.message()))
                } else {
                    onFailure(SuccessResponse(false, authenticationErrorMessage))
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(false, t.message ?: "Unknown error"))
            }
        }
    }

    private inline fun <reified T> singlemap(jsonData: String): T? =
        GsonBuilder().create().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<T>() {}.type)

    private inline fun <reified T> map(jsonData: String): List<T> =
        GsonBuilder().create().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<List<T>>() {}.type)
}
