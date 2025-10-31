package win.downops.wallettracker.data.api

import Cryptography
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.communication.responses.BaseResponse
import win.downops.wallettracker.data.api.communication.responses.CipheredResponse
import win.downops.wallettracker.data.sqlite.session.SessionService
import com.google.gson.GsonBuilder
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseHttpService(context: Context?) {

    private val sessionService = SessionService(context)

    private val privateKey: String
        get() = sessionService.getFirstSession()?.privateKey
            ?: throw Exception("No session found")

    protected val publicKey: String
        get() = sessionService.getFirstSession()?.serverPublicKey
            ?: throw Exception("No session found")

    protected val token: String
        get() = sessionService.getFirstSession()?.token
            ?: throw Exception("No session found")

    protected val cipheredText: String
        get() = Cryptography().sign(privateKey)

    private fun verifySignature(signature: String) = Cryptography().verify(publicKey, signature)

    private fun decryptData(data: CipheredRequest?): String {
        val (encryptedAesKey, iv, ciphertext, tag) = data ?: throw IllegalArgumentException("CipheredRequest is null")
        if (encryptedAesKey.isNullOrEmpty() || iv.isNullOrEmpty() || ciphertext.isNullOrEmpty() || tag.isNullOrEmpty())
            throw IllegalStateException("Invalid CipheredRequest")
        return Cryptography().hybridDecrypt(privateKey, encryptedAesKey, iv, ciphertext, tag)
    }

    protected inline fun <reified R> encryptData(data: R): CipheredRequest? =
        Cryptography().hybridEncrypt(publicKey, GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(data))

    protected fun validateCipheredResponse(response: BaseResponse<CipheredResponse>?): String {
        val ciphered = response?.data ?: throw Exception("No data")
        if (ciphered.signature.isNullOrEmpty()) throw Exception("Invalid data")
        val decrypted = decryptData(ciphered.encrypted_data)
        if (!verifySignature(ciphered.signature)) throw Exception("Invalid signature")
        return decrypted
    }
}
