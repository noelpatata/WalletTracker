package win.downops.wallettracker.data.api.importe

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import jakarta.inject.Inject
import retrofit2.Response
import win.downops.wallettracker.data.ImporteRepository
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.BaseHttpService
import win.downops.wallettracker.data.api.communication.requests.CreateImporteRequest
import win.downops.wallettracker.data.api.communication.requests.ImporteBySeasonIdRequest
import win.downops.wallettracker.data.api.communication.requests.ImporteIdRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Importe
import win.downops.wallettracker.util.Messages.authenticationErrorMessage

@RequiresApi(Build.VERSION_CODES.O)
class ImporteHttpService @Inject constructor(
    sessionRepository: SessionRepository
) : BaseHttpService(sessionRepository), ImporteRepository {

    override suspend fun getBySeasonId(seasonId: Long): AppResult<List<Importe>> {
        return try {
            val cipheredData = encryptData(ImporteBySeasonIdRequest(seasonId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.importe.getBySeasonId("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseListResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching importes", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getById(importeId: Long): AppResult<Importe?> {
        return try {
            val cipheredData = encryptData(ImporteIdRequest(importeId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.importe.getById("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error fetching importe", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun create(importe: Importe): AppResult<Importe?> {
        return try {
            val cipheredData = encryptData(
                CreateImporteRequest(
                    importe.getConcept(),
                    importe.getDateString(),
                    importe.getAmount(),
                    importe.getBalanceAfter(),
                    importe.getSeasonId()
                )
            ) ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.importe.create("Bearer ${getToken()}", getCipheredText(), cipheredData)
            parseObjectResponse(response)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error creating importe", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(importeId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ImporteIdRequest(importeId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.importe.deleteById("Bearer ${getToken()}", getCipheredText(), cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")
            if (body.success) AppResult.Success(body.message, Unit) else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting importe", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteBySeasonId(seasonId: Long): AppResult<Unit> {
        return try {
            val cipheredData = encryptData(ImporteBySeasonIdRequest(seasonId))
                ?: return AppResult.Error(authenticationErrorMessage, isControlled = true)
            val response = ApiClient.importe.deleteBySeasonId("Bearer ${getToken()}", getCipheredText(), cipheredData)
            val body = response.body() ?: return AppResult.Error("No data")
            if (body.success) AppResult.Success(body.message, Unit) else AppResult.Error(body.message)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error deleting importes by season", isControlled = false, e.stackTrace.joinToString("\n"))
        }
    }

    private fun parseObjectResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<Importe?> {
        val body = response.body() ?: return AppResult.Error("No data")
        val json = validateCipheredResponse(body)
        val parsed = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            .fromJson<BaseResponse<Importe?>>(json, object : TypeToken<BaseResponse<Importe?>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data)
        else AppResult.Error(parsed.message)
    }

    private fun parseListResponse(response: Response<BaseResponse<CipheredResponse>>): AppResult<List<Importe>> {
        val body = response.body() ?: return AppResult.Error("No data")
        val json = validateCipheredResponse(body)
        val parsed = GsonBuilder().setDateFormat("yyyy-MM-dd").create()
            .fromJson<BaseResponse<List<Importe>>>(json, object : TypeToken<BaseResponse<List<Importe>>>() {}.type)
        return if (parsed.success) AppResult.Success(parsed.message, parsed.data ?: emptyList())
        else AppResult.Error(parsed.message)
    }
}
