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
import com.example.wallettracker.data.login.AppResult
import com.google.gson.GsonBuilder
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseCategoryDAO(context: Context) : BaseDAO<ExpenseCategory>(context), ExpenseCategoryRepository {

    override suspend fun getAll(): AppResult<List<ExpenseCategory>> {
        return try {
            val response = ApiCall.expenseCategory.getExpenseCategories("Bearer $token", cipheredText)
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", isControlled = false)
        }
    }

    override suspend fun getById(catId: Long): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", isControlled = false)
        }
    }

    override suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryRequest(category))
                ?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.createExpenseCategories("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", isControlled = false)
        }
    }

    override suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryRequest(category))
                ?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.editName("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", isControlled = false)
        }
    }

    override suspend fun deleteById(catId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.deleteById("Bearer $token", cipheredText, cipheredData)
            if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")

            val body = response.body() ?: return AppResult.Error("No data")
            val jsonData = validateCipheredResponse(body)
            val parsed = GsonBuilder().create()
                .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<Unit>>() {}.type) as BaseResponse<Unit>

            if (parsed.success) AppResult.Success(Unit)
            else AppResult.Error(parsed.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", isControlled = false)
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<ExpenseCategory?> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")
        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<ExpenseCategory>>() {}.type) as BaseResponse<ExpenseCategory>

        return if (parsed.success) AppResult.Success(parsed.data)
        else AppResult.Error(parsed.message)
    }

    private fun parseListResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<List<ExpenseCategory>> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")
        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<List<ExpenseCategory>>>() {}.type) as BaseResponse<List<ExpenseCategory>>

        return if (parsed.success) AppResult.Success(parsed.data ?: emptyList())
        else AppResult.Error(parsed.message)
    }
}

