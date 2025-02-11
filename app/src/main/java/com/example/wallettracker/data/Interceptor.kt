package com.example.wallettracker.data

import android.os.Build
import androidx.annotation.RequiresApi
import encrypt
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.IOException

class RSACipherInterceptor : Interceptor {

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest: Request = chain.request()

        // Encrypt "somerandomtext" using the RSA private key
        val cipheredText = try {
            encrypt("somerandomtext")
        } catch (e: Exception) {
            throw IOException("Encryption failed: ${e.message}", e)
        }

        // Add the encrypted text as a query parameter
        val modifiedUrl = originalRequest.url().newBuilder()
            .addQueryParameter("ciphered", cipheredText)
            .build()

        // Build the new request with the modified URL
        val newRequest: Request = originalRequest.newBuilder()
            .url(modifiedUrl)
            .build()

        return chain.proceed(newRequest)
    }
}