package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import BaseDAO
import android.content.Context
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.ExpenseCategoryIdRequest
import com.example.wallettracker.data.communication.ExpenseIdRequest
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.util.Messages.authenticationErrorMessage
import com.example.wallettracker.util.Messages.noDataMessage
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
        encryptData(ExpenseCategoryIdRequest(catId))?.let { cipheredData ->
            ApiCall.expense.getByCatId("Bearer $token", cipheredText, cipheredData).enqueue(handleListResponse(onSuccess, onFailure))
        } ?: onFailure(authenticationErrorMessage)
    }

    override fun create(expense: Expense, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        encryptData(ExpenseRequest(expense, userId))?.let { cipheredData ->
            ApiCall.expense.create("Bearer $token", cipheredText, cipheredData).enqueue(handleResponse(onSuccess, onFailure))
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

    private inline fun <reified T> handleResponse(crossinline onSuccess: (T) -> Unit, crossinline onFailure: (String) -> Unit): Callback<CipheredResponse> {
        return object : Callback<CipheredResponse> {
            override fun onResponse(call: Call<CipheredResponse>, response: Response<CipheredResponse>) {
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

            override fun onFailure(call: Call<CipheredResponse>, t: Throwable) {
                onFailure(t.message ?: "Unknown error")
            }
        }
    }

    private inline fun <reified T> handleListResponse(crossinline onSuccess: (List<T>) -> Unit, crossinline onFailure: (String) -> Unit): Callback<CipheredResponse> {
        return object : Callback<CipheredResponse> {
            override fun onResponse(call: Call<CipheredResponse>, response: Response<CipheredResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val jsonData = verifyData(it)
                        if (jsonData.isNotEmpty()) onSuccess(map<T>(jsonData)) else onFailure(noDataMessage)
                    } ?: onFailure(noDataMessage)
                } else {
                    onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<CipheredResponse>, t: Throwable) {
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
