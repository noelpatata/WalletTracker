package win.downops.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.ApiCall
import BaseDAO
import android.content.Context
import win.downops.wallettracker.data.communication.CipheredResponse
import win.downops.wallettracker.data.communication.ExpenseIdRequest
import win.downops.wallettracker.data.login.AppResult
import win.downops.wallettracker.util.Messages.authenticationErrorMessage
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import retrofit2.Response
import win.downops.wallettracker.data.communication.BaseResponse
import win.downops.wallettracker.data.communication.ExpenseByCategoryIdRequest
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseDAO(context: Context) : BaseDAO<Expense>(context), ExpenseRepository {

    override suspend fun getById(expenseId: Long): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(ExpenseIdRequest(expenseId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.getById("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching expense", isControlled = false)
        }
    }

    override suspend fun getByCatId(catId: Long): AppResult<List<Expense>> {
        return try {
            val cipheredData = encryptData(ExpenseByCategoryIdRequest(catId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.getByCatId("Bearer $token", cipheredText, cipheredData)
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching expenses by category", isControlled = false)
        }
    }

    override suspend fun create(expense: Expense): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(ExpenseRequest(expense, userId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.create("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error creating expense", isControlled = false)
        }
    }

    override suspend fun edit(expense: Expense): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(ExpenseRequest(expense, userId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.edit("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error editing expense", isControlled = false)
        }
    }

    override suspend fun deleteById(expenseId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ExpenseIdRequest(expenseId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiCall.expense.deleteById("Bearer $token", cipheredText, cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")

            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting expense", isControlled = false)
        }
    }

    override suspend fun deleteAll(): AppResult<Unit> {
        return try {
            val response = ApiCall.expense.deleteAll("Bearer $token", cipheredText)
            val body = response.body() ?: return AppResult.Error("No data")

            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting all expenses", isControlled = false)
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<Expense?> {
        if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")
        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)
        val parsed = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<Expense?>>() {}.type) as BaseResponse<Expense>
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
        else AppResult.Error(parsed.message)
    }

    private fun parseListResponse(
        response: Response<BaseResponse<CipheredResponse>>
    ): AppResult<List<Expense>> {
        if (!response.isSuccessful) {
            return AppResult.Error("Network error: ${response.code()}")
        }

        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()

        val parsed = try {
            gson.fromJson(
                jsonData,
                object : com.google.gson.reflect.TypeToken<BaseResponse<List<Expense>>>() {}.type
            ) as BaseResponse<List<Expense>>
        } catch (e: Exception) {
            return AppResult.Error("Parsing error: ${e.message}",
                false,
                e.stackTrace.joinToString("\n"))
        }

        return if (parsed.success) {
            AppResult.Success(parsed.message, parsed.data ?: emptyList())
        } else {
            AppResult.Error(parsed.message)
        }
    }

}
