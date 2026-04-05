package win.downops.wallettracker.data.api

import okhttp3.Interceptor
import okhttp3.Response
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.util.Cryptography
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val sessionRepository: SessionRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val session = sessionRepository.getFirstSession()
        val token = session?.token

        val requestBuilder = originalRequest.newBuilder()
        
        if (token != null && originalRequest.header("Authorization") == null) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        // Add Signature header for authenticated requests if session exists
        if (session != null && originalRequest.header("Signature") == null) {
            val signature = Cryptography.signWithKeystore(BuildConfig.SIGN_SECRET.toByteArray())
            requestBuilder.header("Signature", signature)
        }

        return chain.proceed(requestBuilder.build())
    }
}
