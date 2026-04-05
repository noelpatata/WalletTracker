package win.downops.wallettracker.data.api.expenseCategory

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject
import retrofit2.Response
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.BaseHttpService
import win.downops.wallettracker.data.api.communication.requests.CreateExpenseCategoryRequest
import win.downops.wallettracker.data.api.communication.requests.EditExpenseCategoryRequest
import win.downops.wallettracker.data.api.communication.requests.ExpenseCategoryIdRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseCategoryHttpService @Inject constructor(
    private val apiClient: ApiClient,
    sessionRepository: SessionRepository
) : BaseHttpService(sessionRepository), ExpenseCategoryRepository {

    override suspend fun getAll(): AppResult<List<ExpenseCategory>> {
        return try {
            val response = apiClient.expenseCategory.getExpenseCategories()
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching categories", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getById(catId: Long): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error("Authentication error")
            val response = apiClient.expenseCategory.getExpenseCategoryById(cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching category", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(CreateExpenseCategoryRequest(category.getName()))
                ?: return AppResult.Error("Authentication error")
            val response = apiClient.expenseCategory.createExpenseCategories(cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error creating category", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(
                EditExpenseCategoryRequest(
                    category.getId(),
                    category.getName()
                )
            ) ?: return AppResult.Error("Authentication error")
            val response = apiClient.expenseCategory.editName(cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error editing category", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(catId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error("Authentication error")
            val response = apiClient.expenseCategory.deleteById(cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")
            if (body.success) AppResult.Success(body.message, Unit) else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting category", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<ExpenseCategory?> {
        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson<BaseResponse<ExpenseCategory?>>(jsonData, object : TypeToken<BaseResponse<ExpenseCategory?>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
        else AppResult.Error(parsed.message)
    }

    private fun parseListResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<List<ExpenseCategory>> {
        val body = response.body() ?: return AppResult.Error("No data")
        val jsonData = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson<BaseResponse<List<ExpenseCategory>>>(jsonData, object : TypeToken<BaseResponse<List<ExpenseCategory>>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data ?: emptyList())
        else AppResult.Error(parsed.message)
    }
}
