package win.downops.wallettracker.data.api

import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import com.google.gson.GsonBuilder
import retrofit2.Response
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Session
import win.downops.wallettracker.util.Cryptography

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseHttpService(
    protected val sessionRepository: SessionRepository
) {

    protected val session: Session
        get() = sessionRepository.getFirstSession()
            ?: throw Exception("No session found")

    protected fun getPrivateKey(): String = session.privateKey
    protected fun getPublicKey(): String = session.serverPublicKey
    protected fun getToken(): String = session.token
    protected fun getCipheredText(): String = Cryptography.sign(getPrivateKey())

    private fun verifySignature(signature: String) = Cryptography.verify(getPublicKey(), signature)

    private fun decryptData(data: CipheredRequest?): String {
        val (encryptedAesKey, iv, ciphertext, tag) = data
            ?: throw IllegalArgumentException("CipheredRequest is null")
        if (encryptedAesKey.isNullOrEmpty() || iv.isNullOrEmpty() || ciphertext.isNullOrEmpty() || tag.isNullOrEmpty())
            throw IllegalStateException("Invalid CipheredRequest")
        return Cryptography.hybridDecrypt(getPrivateKey(), encryptedAesKey, iv, ciphertext, tag)
    }

    protected inline fun <reified R> encryptData(data: R): CipheredRequest? =
        Cryptography.hybridEncrypt(getPublicKey(), GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(data))

    protected fun validateCipheredResponse(response: BaseResponse<CipheredResponse>?): String {
        val ciphered = response?.data ?: throw Exception("Unexpected error")
        if (ciphered.signature.isNullOrEmpty()) throw Exception("Invalid signature")
        val decrypted = decryptData(ciphered.encrypted_data)
        if (!verifySignature(ciphered.signature)) throw Exception("Invalid signature")
        return decrypted
    }

    /**
     * Helper for API calls that return CipheredResponse.
     * Automatically attempts to refresh the access token if it receives a 401.
     */
    protected suspend fun <T> safeCipheredApiCall(
        call: suspend (String) -> Response<BaseResponse<CipheredResponse>>,
        parser: (String) -> AppResult<T>
    ): AppResult<T> {
        var response = call(getToken())
        
        if (response.code() == 401) {
            val refreshResult = attemptTokenRefresh()
            if (refreshResult is AppResult.Success) {
                // Retry original call with new token
                response = call(refreshResult.data)
            } else if (refreshResult is AppResult.Error && refreshResult.code == 401) {
                // Refresh token also expired
                return AppResult.Error("Session expired", code = 401)
            }
        }

        if (!response.isSuccessful) {
            return AppResult.Error(response.message(), code = response.code())
        }

        val body = response.body() ?: return AppResult.Error("No data", code = response.code())
        
        return try {
            val jsonData = validateCipheredResponse(body)
            parser(jsonData)
        } catch (e: Exception) {
            AppResult.Error(e.message ?: "Unexpected error", false, e.stackTrace.joinToString("\n"), code = response.code())
        }
    }

    /**
     * Helper for standard API calls (e.g. Delete).
     * Automatically attempts to refresh the access token if it receives a 401.
     */
    protected suspend fun <T> safeApiCall(
        call: suspend (String) -> Response<BaseResponse<T>>
    ): AppResult<T?> {
        var response = call(getToken())

        if (response.code() == 401) {
            val refreshResult = attemptTokenRefresh()
            if (refreshResult is AppResult.Success) {
                response = call(refreshResult.data)
            } else if (refreshResult is AppResult.Error && refreshResult.code == 401) {
                return AppResult.Error("Session expired", code = 401)
            }
        }

        val body = response.body()
        return if (response.isSuccessful && body != null) {
            if (body.success) AppResult.Success(body.message, body.data)
            else AppResult.Error(body.message, code = response.code())
        } else {
            AppResult.Error(response.message(), code = response.code())
        }
    }

    private suspend fun attemptTokenRefresh(): AppResult<String> {
        val refreshResponse = ApiClient.login.refresh("Bearer ${getToken()}")
        if (refreshResponse.isSuccessful && refreshResponse.body()?.success == true) {
            val newToken = refreshResponse.body()?.data?.token
            if (newToken != null) {
                val currentSession = session
                currentSession.token = newToken
                sessionRepository.edit(currentSession)
                return AppResult.Success("Token refreshed", newToken)
            }
        }
        return AppResult.Error("Refresh failed", code = refreshResponse.code())
    }
}
