package win.downops.wallettracker.data.api.login

import com.google.gson.GsonBuilder
import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.LoginRepository
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.LoginResponse
import win.downops.wallettracker.data.api.communication.responses.ServerPubKeyResponse
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.util.Messages.errorFetchingPublicKey
import win.downops.wallettracker.util.Messages.errorSendingPublicKey
import win.downops.wallettracker.util.Messages.loginFailedMessage
import javax.inject.Inject

class LoginHttpService @Inject constructor(): LoginRepository {

    override suspend fun login(credentials: LoginRequest): AppResult<LoginResponse?> {
        val response = ApiClient.login.login(credentials)
        val body = response.body() ?: return AppResult.Error("No data")

        return if (body.success) AppResult.Success(body.message, body.data)
        else AppResult.Error(body.message)
    }

    override suspend fun setUserClientPubKey(token: String, request: ServerPubKeyRequest): AppResult<Unit> {
        val response = ApiClient.login.setUserClientPubKey("Bearer $token", request)
        val body = response.body() ?: return AppResult.Error("No data")

        return if (body.success) AppResult.Success(body.message, Unit)
        else AppResult.Error(body.message)
    }

    override suspend fun getUserServerPubKey(token: String): AppResult<ServerPubKeyResponse?> {
        val response = ApiClient.login.getUserServerPubKey("Bearer $token")
        val body = response.body() ?: return AppResult.Error("No data")

        return if (body.success) AppResult.Success(body.message, body.data)
        else AppResult.Error(body.message)
    }
}