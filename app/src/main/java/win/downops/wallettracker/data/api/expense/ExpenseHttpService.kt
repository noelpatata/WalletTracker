package win.downops.wallettracker.data.api.expense

import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.BaseHttpService
import android.content.Context
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import win.downops.wallettracker.data.api.communication.requests.ExpenseIdRequest
import win.downops.wallettracker.data.api.communication.requests.CreateExpenseRequest
import win.downops.wallettracker.data.api.communication.requests.EditExpenseRequest
import win.downops.wallettracker.data.api.communication.requests.ExpenseByCategoryIdRequest
import win.downops.wallettracker.util.Messages.authenticationErrorMessage
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import retrofit2.Response
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.api.communication.responses.BaseResponse

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseHttpService @Inject constructor(
    sessionRepository: SessionRepository
) : BaseHttpService(sessionRepository),
    ExpenseRepository{

    override suspend fun getById(expenseId: Long): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(
                ExpenseIdRequest(
                    expenseId
                )
            )?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiClient.expense.getById("Bearer ${this.getToken()}", this.getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error fetching expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getByCatId(catId: Long): AppResult<List<Expense>> {
        return try {
            val cipheredData = encryptData(
                ExpenseByCategoryIdRequest(
                    catId
                )
            )?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiClient.expense.getByCatId("Bearer ${this.getToken()}", this.getCipheredText(), cipheredData)
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error fetching expenses by category",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun create(expense: Expense): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(
                CreateExpenseRequest(
                    expense.getPrice(),
                    expense.getDateString(),
                    expense.getCategoryId(),
                    expense.getDescription()
                )
            ) ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiClient.expense.create("Bearer ${this.getToken()}", this.getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error creating expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun edit(expense: Expense): AppResult<Expense?> {
        return try {
            val cipheredData = encryptData(
                EditExpenseRequest(
                    expense.getId(),
                    expense.getPrice(),
                    expense.getDateString(),
                    expense.getCategoryId(),
                    expense.getDescription()
                )
            )?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiClient.expense.edit("Bearer ${this.getToken()}", this.getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error editing expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(expenseId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ExpenseIdRequest(expenseId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)

            val response = ApiClient.expense.deleteById("Bearer ${this.getToken()}", this.getCipheredText(), cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")

            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error deleting expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteAll(): AppResult<Unit> {
        return try {
            val response = ApiClient.expense.deleteAll("Bearer ${this.getToken()}", this.getCipheredText())
            val body = response.body() ?: return AppResult.Error("No data")

            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error deleting all expenses",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<Expense?> {
        try{
            val body = response.body() ?: return AppResult.Error("No data")
            val jsonData = validateCipheredResponse(body)
            val parsed = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
                .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<Expense?>>() {}.type) as BaseResponse<Expense>
            return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
            else AppResult.Error(parsed.message)
        }catch (e: Exception) {
            throw e
        }

    }

    private fun parseListResponse(
        response: Response<BaseResponse<CipheredResponse>>
    ): AppResult<List<Expense>> {
        try{
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
        }catch (e: Exception) {
            throw e
        }

    }

}
