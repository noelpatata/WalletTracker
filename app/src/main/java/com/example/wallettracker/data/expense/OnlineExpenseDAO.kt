package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import BaseDAO
import android.content.Context
import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.ExpenseIdRequest
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.interfaces.ExpenseRepository
import com.example.wallettracker.util.Constantes.authenticationErrorMessage
import com.example.wallettracker.util.Constantes.noDataMessage
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseDAO(context: Context) : BaseDAO<Expense>(context), ExpenseRepository {

    override fun getById(expenseId: Long, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        encryptData(ExpenseIdRequest(expenseId))?.let { cipheredData ->
            ApiCall.expense.getById("Bearer $token", cipheredText, cipheredData).enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun getByCatId(catId: Long, onSuccess: (List<Expense>) -> Unit, onFailure: (String) -> Unit) {
        encryptData(CatIdRequest(catId))?.let { cipheredData ->
            ApiCall.expense.getByCatId("Bearer $token", cipheredText, cipheredData).enqueue(handleListResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun createExpense(expense: Expense, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        encryptData(ExpenseRequest(expense, userId))?.let { cipheredData ->
            ApiCall.expense.createExpense("Bearer $token", cipheredText, cipheredData).enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun edit(expense: Expense, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        encryptData(ExpenseRequest(expense, userId))?.let { cipheredData ->
            ApiCall.expense.edit("Bearer $token", cipheredText, cipheredData).enqueue(handleResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun deleteById(expenseId: Long, onSuccess: (SuccessResponse) -> Unit, onFailure: (String) -> Unit) {
        encryptData(ExpenseIdRequest(expenseId))?.let { cipheredData ->
            ApiCall.expense.deleteById("Bearer $token", cipheredText, cipheredData).enqueue(handleSuccessResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun deleteAll(onSuccess: (SuccessResponse) -> Unit, onFailure: (String) -> Unit) {
        ApiCall.expense.deleteAll("Bearer $token", cipheredText).enqueue(handleSuccessResponse(onSuccess, onFailure))
    }

    private inline fun <reified T> handleResponse(crossinline onSuccess: (T) -> Unit, crossinline onFailure: (String) -> Unit): Callback<DataResponse> {
        return object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val jsonData = verifyData(it)
                        if (jsonData.isNotEmpty()) singlemap<T>(jsonData)?.let(onSuccess) ?: onFailure(noDataMessage)
                        else onFailure(authenticationErrorMessage)
                    } ?: onFailure(noDataMessage)
                } else {
                    onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(t.message ?: "Unknown error")
            }
        }
    }

    private inline fun <reified T> handleListResponse(crossinline onSuccess: (List<T>) -> Unit, crossinline onFailure: (String) -> Unit): Callback<DataResponse> {
        return object : Callback<DataResponse> {
            override fun onResponse(call: Call<DataResponse>, response: Response<DataResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val jsonData = verifyData(it)
                        if (jsonData.isNotEmpty()) onSuccess(map<T>(jsonData)) else onFailure(noDataMessage)
                    } ?: onFailure(noDataMessage)
                } else {
                    onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(t.message ?: "Unknown error")
            }
        }
    }

    private fun handleSuccessResponse(onSuccess: (SuccessResponse) -> Unit, onFailure: (String) -> Unit): Callback<SuccessResponse> {
        return object : Callback<SuccessResponse> {
            override fun onResponse(call: Call<SuccessResponse>, response: Response<SuccessResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let(onSuccess) ?: onFailure(response.message())
                } else {
                    onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(t.message ?: "Unknown error")
            }
        }
    }

    private inline fun <reified T> singlemap(jsonData: String): T? {
        return GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<T>() {}.type)
    }

    private inline fun <reified T> map(jsonData: String): List<T> {
        return GsonBuilder().setDateFormat("yyyy-MM-dd").create().fromJson(jsonData, object : com.google.gson.reflect.TypeToken<List<T>>() {}.type)
    }
}
