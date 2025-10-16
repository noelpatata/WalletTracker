package win.downops.wallettracker.data.login

import win.downops.wallettracker.data.ApiCall
import win.downops.wallettracker.util.Messages.errorFetchingPublicKey
import win.downops.wallettracker.util.Messages.errorSendingPublicKey
import win.downops.wallettracker.util.Messages.loginFailedMessage

class LoginDAO {

    suspend fun login(credentials: LoginRequest): LoginResponse {
        val response = ApiCall.login.login(credentials)
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
        val response = ApiCall.login.setUserClientPubKey("Bearer $token", request)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            throw Exception(body?.message ?: errorSendingPublicKey)
        }
    }

    suspend fun getUserServerPubKey(token: String): ServerPubKeyResponse {
        val response = ApiCall.login.getUserServerPubKey("Bearer $token")
        val body = response.body()
        if (response.isSuccessful && body?.success == true && body.data != null) {
            return body.data
        } else {
            throw Exception(body?.message ?: errorFetchingPublicKey)
        }
    }
}