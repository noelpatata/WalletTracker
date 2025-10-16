package win.downops.wallettracker.data.online.expenseCategory
import win.downops.wallettracker.data.online.BaseOnlineDAO
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.online.ApiCall
import win.downops.wallettracker.data.online.communication.responses.BaseResponse
import win.downops.wallettracker.data.online.communication.responses.CipheredResponse
import win.downops.wallettracker.data.online.communication.requests.ExpenseCategoryIdRequest
import win.downops.wallettracker.data.online.communication.requests.CreateExpenseCategoryRequest
import win.downops.wallettracker.data.online.communication.requests.EditExpenseCategoryRequest
import com.google.gson.GsonBuilder
import retrofit2.Response
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory

@RequiresApi(Build.VERSION_CODES.O)
class OnlineExpenseCategoryDAO(context: Context) : BaseOnlineDAO<ExpenseCategory>(context),
    ExpenseCategoryRepository {

    override suspend fun getAll(): AppResult<List<ExpenseCategory>> {
        return try {
            val response = ApiCall.expenseCategory.getExpenseCategories("Bearer $token", cipheredText)
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getById(catId: Long): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(ExpenseCategoryIdRequest(catId))
                ?: return AppResult.Error("Authentication error")
            val response = ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun create(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(CreateExpenseCategoryRequest(category.getName()))
                ?: return AppResult.Error("Authentication error")


            val response = ApiCall.expenseCategory.createExpenseCategories("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun edit(category: ExpenseCategory): AppResult<ExpenseCategory?> {
        return try {
            val cipheredData = encryptData(
                EditExpenseCategoryRequest(
                    category.getId(),
                    category.getName()
                )
            )?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.editName("Bearer $token", cipheredText, cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(catId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(
                ExpenseCategoryIdRequest(
                    catId
                )
            )?: return AppResult.Error("Authentication error")

            val response = ApiCall.expenseCategory.deleteById("Bearer $token", cipheredText, cipheredData)
            if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")

            val body = response.body() ?: return AppResult.Error("No data")

            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<ExpenseCategory?> {
        try{
            if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")
            val body = response.body() ?: return AppResult.Error("No data")
            val jsonData = validateCipheredResponse(body)
            val parsed = GsonBuilder().create()
                .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<ExpenseCategory?>>() {}.type) as BaseResponse<ExpenseCategory>
            return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
            else AppResult.Error(parsed.message)
        } catch (e: Exception) {
            throw e
        }

    }

    private fun parseListResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<List<ExpenseCategory>> {
        try{
            if (!response.isSuccessful) return AppResult.Error("Network error: ${response.code()}")
            val body = response.body() ?: return AppResult.Error("No data")
            val jsonData = validateCipheredResponse(body)
            val parsed = GsonBuilder().create()
                .fromJson(jsonData, object : com.google.gson.reflect.TypeToken<BaseResponse<List<ExpenseCategory>>>() {}.type) as BaseResponse<List<ExpenseCategory>>

            return if (parsed.success) AppResult.Success(parsed.message, parsed.data ?: emptyList())
            else AppResult.Error(parsed.message)
        }catch (e: Exception) {
            throw e
        }

    }
}

