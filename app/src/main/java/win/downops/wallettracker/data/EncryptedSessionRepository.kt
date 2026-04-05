package win.downops.wallettracker.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import win.downops.wallettracker.data.models.Session
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SessionRepository {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun edit(session: Session): Int {
        prefs.edit().apply {
            putString("access_token", session.token)
            putString("username", session.username)
            putString("server_public_key", session.serverPublicKey)
            putString("encrypted_credentials", session.encryptedCredentials)
            putString("biometrics_credentials", session.biometricsCredentials)
            putString("biometrics_iv", session.biometricsIv)
            putBoolean("fingerPrint", session.fingerPrint)
            putBoolean("online", session.online)
            apply()
        }
        return 1
    }

    override fun insert(session: Session): Long {
        edit(session)
        return 1L
    }

    override fun deleteAll() {
        prefs.edit().clear().apply()
    }

    override fun getFirstSession(): Session? {
        val token = prefs.getString("access_token", null) ?: return null
        return Session().apply {
            this.token = token
            this.username = prefs.getString("username", "") ?: ""
            this.serverPublicKey = prefs.getString("server_public_key", "") ?: ""
            this.encryptedCredentials = prefs.getString("encrypted_credentials", "") ?: ""
            this.biometricsCredentials = prefs.getString("biometrics_credentials", "") ?: ""
            this.biometricsIv = prefs.getString("biometrics_iv", "") ?: ""
            this.fingerPrint = prefs.getBoolean("fingerPrint", false)
            this.online = prefs.getBoolean("online", false)
        }
    }

    override fun close() {
        // No-op
    }
}
