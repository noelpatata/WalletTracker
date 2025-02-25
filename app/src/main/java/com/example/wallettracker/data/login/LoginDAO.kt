package com.example.wallettracker.data.login

import Cryptography
import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.session.SessionDAO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginDAO(private val credentials: LoginRequest) {

    fun login(onSuccess: (LoginResponse) -> Unit, onFailure: (String) -> Unit) {
        val credentials = "${credentials.username}:${credentials.password}"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        ApiCall.login.login(basicAuth).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val loginReponse = response.body()
                    loginReponse?.let{
                        onSuccess(it)
                    }

                } else {
                    onFailure("Login failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onFailure("${t.message}")
            }
        })
    }
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun autologin(context: Context, onSuccess: (LoginResponse) -> Unit, onFailure: (SuccessResponse) -> Unit) {
            SessionDAO(context).use { sSess ->
                val sess = sSess.getFirstSession()
                val userId = sess!!.userId
                val signatureb64 = Cryptography(context, userId).sign() //encrypts with private key
                val requestBody = AutoLoginRequest(userId, signatureb64)
                ApiCall.login.autologin(requestBody).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginReponse = response.body()
                            loginReponse?.let{
                                onSuccess(it)
                            }

                        } else {
                            onFailure(SuccessResponse(success = false, message = response.message()))
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        onFailure(SuccessResponse(success = false, message = t.message.toString()))
                    }
                })
            }

        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun setUserClientPubKey(request: ServerPubKeyRequest,
                            onSuccess: (SuccessResponse) -> Unit,
                            onFailure: (SuccessResponse) -> Unit) {
        ApiCall.login.setUserClientPubKey(request).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>) {
                if (response.isSuccessful) {
                    val loginResponse = response.body()
                    if(loginResponse?.success == true){
                        onSuccess(loginResponse)
                    }
                    else{
                        loginResponse?.let { onFailure(it) }
                    }

                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserServerPubKey(request: LoginRequest,
                            onSuccess: (ServerPubKeyResponse) -> Unit,
                            onFailure: (SuccessResponse) -> Unit) {
        ApiCall.login.getUserServerPubKey(request).enqueue(object : Callback<ServerPubKeyResponse> {
            override fun onResponse(
                call: Call<ServerPubKeyResponse>,
                response: Response<ServerPubKeyResponse>
            ) {
                if (response.isSuccessful) {
                    val loginReponse = response.body()
                    loginReponse?.let{
                        onSuccess(it)
                    }

                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<ServerPubKeyResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }




}
