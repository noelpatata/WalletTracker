package win.downops.wallettracker.data.api.login

import win.downops.wallettracker.data.api.ApiClient
import win.downops.wallettracker.data.api.communication.requests.LoginRequest
import win.downops.wallettracker.data.api.communication.requests.ServerPubKeyRequest
import win.downops.wallettracker.data.api.communication.responses.LoginResponse
import win.downops.wallettracker.data.api.communication.responses.ServerPubKeyResponse
import win.downops.wallettracker.util.Messages.errorFetchingPublicKey
import win.downops.wallettracker.util.Messages.errorSendingPublicKey
import win.downops.wallettracker.util.Messages.loginFailedMessage

class LoginHttpService {

    suspend fun login(credentials: LoginRequest): LoginResponse {
        val response = ApiClient.login.login(credentials)
        if (response.isSuccessful) {
            val body = response.body()
            if (body?.success == true && body.data != null) {
                return body.data
            } else {
                throw Exception(body?.message ?: loginFailedMessage)
            }
        } else {
            throw Exception("${response.code()}")
        }
    }

    suspend fun setUserClientPubKey(token: String, request: ServerPubKeyRequest) {
        val response = ApiClient.login.setUserClientPubKey("Bearer $token", request)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw Exception(body?.message ?: errorSendingPublicKey)
        }
    }

    suspend fun getUserServerPubKey(token: String): ServerPubKeyResponse {
        val response = ApiClient.login.getUserServerPubKey("Bearer $token")
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            return body.data
        } else {
            throw Exception(body?.message ?: errorFetchingPublicKey)
        }
    }
}