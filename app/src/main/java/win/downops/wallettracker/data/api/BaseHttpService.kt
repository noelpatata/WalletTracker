package win.downops.wallettracker.data.api

import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import com.google.gson.GsonBuilder
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.SessionRepository
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import win.downops.wallettracker.data.models.Session
import win.downops.wallettracker.util.Cryptography

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseHttpService(
    protected val sessionRepository: SessionRepository
) {

    protected val session: Session
        get() = sessionRepository.getFirstSession()
            ?: throw Exception("No session found")

    protected fun getPublicKey(): String = session.serverPublicKey
    protected fun getToken(): String = session.token
    
    protected fun getCipheredText(): String = 
        Cryptography.signWithKeystore(BuildConfig.SIGN_SECRET.toByteArray())

    private fun verifySignature(signature: String) = Cryptography.verify(getPublicKey(), signature)

    private fun decryptData(data: CipheredRequest?): String {
        val (encryptedAesKey, iv, ciphertext, tag) = data
            ?: throw IllegalArgumentException("CipheredRequest is null")
        if (encryptedAesKey.isNullOrEmpty() || iv.isNullOrEmpty() || ciphertext.isNullOrEmpty() || tag.isNullOrEmpty())
            throw IllegalStateException("Invalid CipheredRequest")
        return Cryptography.decryptWithKeystore(encryptedAesKey, iv, ciphertext, tag)
    }

    protected inline fun <reified R> encryptData(data: R): CipheredRequest? =
        Cryptography.hybridEncrypt(getPublicKey(), GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(data))

    protected fun validateCipheredResponse(response: BaseResponse<CipheredResponse>?): String {
        val ciphered = response?.data ?: throw Exception("Unexpected error")
        if (ciphered.signature.isNullOrEmpty()) throw Exception("Invalid signature")
        val decrypted = decryptData(ciphered.encrypted_data)
        if (!verifySignature(ciphered.signature)) throw Exception("Invalid signature")
        return decrypted
    }
}
