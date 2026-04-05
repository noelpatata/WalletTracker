package win.downops.wallettracker.data.api

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.login.LoginEndpoints
import win.downops.wallettracker.util.Logger
import javax.inject.Inject
import javax.inject.Provider

class TokenAuthenticator @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val loginEndpointsProvider: Provider<LoginEndpoints>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite loops
        if (response.request.header("Authorization-Retry") != null) return null

        val session = sessionRepository.getFirstSession() ?: return null
        val expiredToken = session.token
        if (expiredToken.isEmpty()) return null

        synchronized(this) {
            val currentSession = sessionRepository.getFirstSession()
            val tokenToUse = if (currentSession?.token != expiredToken) {
                // Token was already refreshed by another thread
                currentSession?.token
            } else {
                val loginEndpoints = loginEndpointsProvider.get()
                try {
                    val refreshResponse = loginEndpoints.refreshSync("Bearer $expiredToken").execute()
                    if (refreshResponse.isSuccessful) {
                        val newToken = refreshResponse.body()?.data?.token
                        if (newToken != null) {
                            currentSession.apply { token = newToken }
                            sessionRepository.edit(currentSession)
                            newToken
                        } else null
                    } else {
                        Logger.log("Refresh token failed: ${refreshResponse.code()}")
                        null
                    }
                } catch (e: Exception) {
                    Logger.log("Refresh token exception: ${e.message}")
                    null
                }
            }

            return if (tokenToUse != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $tokenToUse")
                    .header("Authorization-Retry", "true")
                    .build()
            } else {
                null
            }
        }
    }
}
