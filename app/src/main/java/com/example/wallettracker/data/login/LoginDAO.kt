package com.example.wallettracker.data.login

import Cryptography
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.session.Session
import com.example.wallettracker.util.Messages.errorFetchingPublicKey
import com.example.wallettracker.util.Messages.errorSendingPublicKey
import com.example.wallettracker.util.Messages.loginFailedMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginDAO(private val credentials: LoginRequest) {

    fun login(onSuccess: (LoginResponse) -> Unit, onFailure: (String) -> Unit) {

        ApiCall.login.login(credentials).enqueue(object : Callback<BaseResponse<LoginResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<LoginResponse>>,
                response: Response<BaseResponse<LoginResponse>>
            ) {
                val baseResponse = response.body()
                if (baseResponse != null && baseResponse.success && baseResponse.data != null) {
                    onSuccess(baseResponse.data)
                } else {
                    onFailure(baseResponse?.message ?: loginFailedMessage)
                }
            }

            override fun onFailure(call: Call<BaseResponse<LoginResponse>>, t: Throwable) {
                onFailure(loginFailedMessage)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setUserClientPubKey(token: String,
                            request: ServerPubKeyRequest,
                            onSuccess: () -> Unit,
                            onFailure: (String) -> Unit) {
        ApiCall.login.setUserClientPubKey("Bearer $token", request).enqueue(object : Callback<BaseResponse<Nothing>> {
            override fun onResponse(
                call: Call<BaseResponse<Nothing>>,
                response: Response<BaseResponse<Nothing>>) {
                val baseResponse = response.body()
                if (baseResponse != null && baseResponse.success) {
                    onSuccess()
                } else {
                    onFailure(baseResponse?.message ?: errorSendingPublicKey)
                }
            }

            override fun onFailure(call: Call<BaseResponse<Nothing>>, t: Throwable) {
                onFailure(errorSendingPublicKey)
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserServerPubKey(token: String,
                            onSuccess: (ServerPubKeyResponse) -> Unit,
                            onFailure: (String) -> Unit) {
        ApiCall.login.getUserServerPubKey("Bearer $token").enqueue(object : Callback<BaseResponse<ServerPubKeyResponse>> {
            override fun onResponse(
                call: Call<BaseResponse<ServerPubKeyResponse>>,
                response: Response<BaseResponse<ServerPubKeyResponse>>
            ) {
                val baseResponse = response.body()
                if (baseResponse != null && baseResponse.success && baseResponse.data != null) {
                    onSuccess(baseResponse.data)
                } else {
                    onFailure(baseResponse?.message ?: loginFailedMessage)
                }
            }

            override fun onFailure(call: Call<BaseResponse<ServerPubKeyResponse>>, t: Throwable) {
                onFailure(errorFetchingPublicKey)
            }
        })
    }




}
