package win.downops.wallettracker.data

import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.api.communication.responses.LoginResponse
import win.downops.wallettracker.data.api.communication.responses.ServerPubKeyResponse
import win.downops.wallettracker.data.models.AppResult

interface LoginRepository {
    suspend fun login(credentials: LoginRequest): AppResult<LoginResponse?>
    suspend fun setUserClientPubKey(token: String, request: ServerPubKeyRequest): AppResult<Unit>
    suspend fun getUserServerPubKey(token: String): AppResult<ServerPubKeyResponse?>
}