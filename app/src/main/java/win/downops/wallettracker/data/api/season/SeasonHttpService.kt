package win.downops.wallettracker.data.api.season

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject
import retrofit2.Response
import win.downops.wallettracker.data.SeasonRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.BaseHttpService
import win.downops.wallettracker.data.api.communication.requests.GetOrCreateSeasonRequest
import win.downops.wallettracker.data.api.communication.requests.SeasonIdRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Season
import win.downops.wallettracker.util.Messages.authenticationErrorMessage

@RequiresApi(Build.VERSION_CODES.O)
class SeasonHttpService @Inject constructor(
    sessionRepository: SessionRepository
) : BaseHttpService(sessionRepository), SeasonRepository {

    override suspend fun getAll(): AppResult<List<Season>> {
        return try {
            val response = ApiClient.season.getAll("Bearer ${getToken()}", getCipheredText())
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching seasons", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getById(seasonId: Long): AppResult<Season?> {
        return try {
            val cipheredData = encryptData(SeasonIdRequest(seasonId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.season.getById("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching season", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getByYearMonth(year: Int, month: Int): AppResult<Season?> {
        return try {
            val cipheredData = encryptData(GetOrCreateSeasonRequest(year, month))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.season.getOrCreate("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching season", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getOrCreate(year: Int, month: Int): AppResult<Season?> {
        return try {
            val cipheredData = encryptData(GetOrCreateSeasonRequest(year, month))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.season.getOrCreate("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error creating season", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(seasonId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(SeasonIdRequest(seasonId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.season.deleteById("Bearer ${getToken()}", getCipheredText(), cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")
            if (body.success) AppResult.Success(body.message, Unit)
            else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting season", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<Season?> {
        val body = response.body() ?: return AppResult.Error("No data")
        val json = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson<BaseResponse<Season?>>(json, object : TypeToken<BaseResponse<Season?>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
        else AppResult.Error(parsed.message)
    }

    private fun parseListResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<List<Season>> {
        val body = response.body() ?: return AppResult.Error("No data")
        val json = validateCipheredResponse(body)
        val parsed = GsonBuilder().create()
            .fromJson<BaseResponse<List<Season>>>(json, object : TypeToken<BaseResponse<List<Season>>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data ?: emptyList())
        else AppResult.Error(parsed.message)
    }
}
