import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.communication.BaseResponse
import com.example.wallettracker.data.communication.CipheredRequest
import com.example.wallettracker.data.communication.CipheredResponse
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.util.Constantes.invalidData
import com.example.wallettracker.util.Constantes.invalidSignature
import com.example.wallettracker.util.Constantes.noDataMessage
import com.google.gson.GsonBuilder

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseDAO<T>(context: Context) {
    protected lateinit var cipheredText: String
    protected lateinit var privateKey: String
    protected lateinit var publicKey: String
    protected lateinit var token: String
    protected var userId: Int = 0

    init {
        SessionDAO(context).getFirstSession()?.let {
            cipheredText = Cryptography().sign(it.privateKey)
            token = it.token
            userId = it.userId
            privateKey = it.privateKey
            publicKey = it.serverPublicKey
        } ?: throw Exception("No session found")
    }

    protected fun verifySignature(signature: String) = Cryptography().verify(publicKey, signature)

    protected fun decryptData(data: CipheredRequest) = Cryptography().hybridDecrypt(
        privateKey, data.encrypted_aes_key, data.iv, data.ciphertext, data.tag
    )

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

    protected fun validateCipheredResponse(
        response: BaseResponse<CipheredResponse>?,
        onFailure: (SuccessResponse) -> Unit
    ): String? {
        if (response?.data == null) {
            onFailure(SuccessResponse(false, noDataMessage))
            return null
        }

        val ciphered = response.data
        if (ciphered.signature.isEmpty()) {
            onFailure(SuccessResponse(false, invalidData))
            return null
        }

        val decrypted = decryptData(ciphered.encrypted_data)
        if (decrypted.isEmpty()) {
            onFailure(SuccessResponse(false, noDataMessage))
            return null
        }

        if (!verifySignature(ciphered.signature)) {
            onFailure(SuccessResponse(false, invalidSignature))
            return null
        }

        return decrypted
    }
}
