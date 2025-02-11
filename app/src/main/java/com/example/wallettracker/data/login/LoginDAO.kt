package com.example.wallettracker.data.login

import Cryptography
import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.SuccessResponse
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
        fun autologin(context: Context, userId:Int, onSuccess: (LoginResponse) -> Unit, onFailure: (SuccessResponse) -> Unit) {
            val ciphered = Cryptography(context, userId).encrypt("somerandomtext") //encrypts with private key
            ApiCall.login.autologin(userId, ciphered).enqueue(object : Callback<LoginResponse> {
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
