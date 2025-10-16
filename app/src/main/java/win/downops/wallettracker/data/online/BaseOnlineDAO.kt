package win.downops.wallettracker.data.online

import Cryptography
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.online.communication.responses.BaseResponse
import win.downops.wallettracker.data.online.communication.responses.CipheredResponse
import win.downops.wallettracker.data.session.SessionDAO
import win.downops.wallettracker.util.Messages.invalidData
import win.downops.wallettracker.util.Messages.invalidSignature
import win.downops.wallettracker.util.Messages.noDataMessage
import com.google.gson.GsonBuilder
import win.downops.wallettracker.data.online.communication.requests.CipheredRequest

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseOnlineDAO<T>(context: Context) {
    private lateinit var privateKey: String
    protected lateinit var publicKey: String
    protected lateinit var cipheredText: String
    protected lateinit var token: String
    protected var userId: Int = 0

    init {
        SessionDAO(context).getFirstSession()?.let {
            cipheredText = Cryptography().sign(it.privateKey)
            token = it.token
            privateKey = it.privateKey
            publicKey = it.serverPublicKey
        } ?: throw Exception("No session found")
    }

    private fun verifySignature(signature: String) = Cryptography().verify(publicKey, signature)

    private fun decryptData(data: CipheredRequest?): String {
        if (data == null) {
            throw IllegalArgumentException("CipheredRequest is null — cannot decrypt.")
        }

        val (encryptedAesKey, iv, ciphertext, tag) = data
        if (encryptedAesKey.isNullOrEmpty() ||
            iv.isNullOrEmpty() ||
            ciphertext.isNullOrEmpty() ||
            tag.isNullOrEmpty()
        ) {
            throw IllegalStateException(
                "Invalid CipheredRequest: one or more fields are missing or empty. " +
                        "AES key: ${!encryptedAesKey.isNullOrEmpty()}, iv: ${!iv.isNullOrEmpty()}, " +
                        "ciphertext: ${!ciphertext.isNullOrEmpty()}, tag: ${!tag.isNullOrEmpty()}"
            )
        }

        return Cryptography().hybridDecrypt(
            privateKey,
            encryptedAesKey,
            iv,
            ciphertext,
            tag
        )
    }


    protected fun verifyData(response: CipheredResponse?): String {
        return if (response?.signature != null && verifySignature(response.signature)) {
            decryptData(response.encrypted_data)
        } else {
            ""
        }
    }


    protected inline fun <reified R> encryptData(data: R): CipheredRequest? {
        return Cryptography().hybridEncrypt(publicKey, GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(data))
    }

    protected fun validateCipheredResponse(response: BaseResponse<CipheredResponse>?): String {

        val ciphered = response?.data ?: throw Exception(noDataMessage)
        if (ciphered.signature.isNullOrEmpty()) {
            throw Exception(invalidData)
        }

        val decrypted = decryptData(ciphered.encrypted_data)
        if (decrypted.isEmpty()) {
            throw Exception(noDataMessage)
        }

        if (!verifySignature(ciphered.signature)) {
            throw Exception(invalidSignature)
        }

        return decrypted
    }

}
