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
import com.example.wallettracker.data.login.AppResult
import com.example.wallettracker.util.Messages.authenticationErrorMessage
import com.example.wallettracker.util.Messages.noDataMessage
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseDAO(context: Context) : BaseDAO<Expense>(context), ExpenseRepository {

    override suspend fun getById(expenseId: Long): AppResult<Expense> {
        return try {
            val cipheredData = encryptData(ExpenseIdRequest(expenseId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.getById("Bearer $token", cipheredText, cipheredData)
            handleObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching expense", isControlled = false)
        }
    }

    override suspend fun getByCatId(catId: Long): AppResult<List<Expense>> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.getByCatId("Bearer $token", cipheredText, cipheredData)
            handleListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching expenses by category", isControlled = false)
        }
    }

    override suspend fun create(expense: Expense): AppResult<Expense> {
        return try {
            val cipheredData = encryptData(ExpenseRequest(expense, userId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.create("Bearer $token", cipheredText, cipheredData)
            handleObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error creating expense", isControlled = false)
        }
    }

    override suspend fun edit(expense: Expense): AppResult<Expense> {
        return try {
            val cipheredData = encryptData(ExpenseRequest(expense, userId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.edit("Bearer $token", cipheredText, cipheredData)
            handleObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error editing expense", isControlled = false)
        }
    }

    override suspend fun deleteById(expenseId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ExpenseIdRequest(expenseId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.deleteById("Bearer $token", cipheredText, cipheredData)
            handleSuccessResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting expense", isControlled = false)
        }
    }

    override suspend fun deleteAll(): AppResult<Unit> {
        return try {
            val response = ApiCall.expense.deleteAll("Bearer $token", cipheredText)
            handleSuccessResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting all expenses", isControlled = false)
        }
    }

    private inline fun <reified T> handleObjectResponse(response: Response<CipheredResponse>): AppResult<T> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}", isControlled = true)
        val body = response.body() ?: return AppResult.Error(noDataMessage, isControlled = true)
        val jsonData = verifyData(body)
        if (jsonData.isEmpty()) return AppResult.Error(authenticationErrorMessage, isControlled = true)
        val deserialized = deserialize<T>(jsonData)
        return deserialized?.let { AppResult.Success(it) } ?: AppResult.Error(noDataMessage, isControlled = true)
    }

    private inline fun <reified T> handleListResponse(response: Response<CipheredResponse>): AppResult<List<T>> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}", isControlled = true)
        val body = response.body() ?: return AppResult.Error(noDataMessage, isControlled = true)
        val jsonData = verifyData(body)
        if (jsonData.isEmpty()) return AppResult.Error(authenticationErrorMessage, isControlled = true)
        val list = deserializeList<T>(jsonData)
        return AppResult.Success(list)
    }

    private fun handleSuccessResponse(response: Response<SuccessResponse>): AppResult<Unit> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}", isControlled = true)
        val body = response.body() ?: return AppResult.Error(noDataMessage, isControlled = true)
        return if (body.success) AppResult.Success(Unit) else AppResult.Error(body.message, isControlled = true)
    }

    private inline fun <reified T> deserialize(jsonData: String): T? {
        return GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<T>() {}.type)
    }

    private inline fun <reified T> deserializeList(jsonData: String): List<T> {
        return GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<List<T>>() {}.type)
            ?: emptyList()
    }
}
