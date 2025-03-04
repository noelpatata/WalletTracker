import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SymmetricResponse
import com.example.wallettracker.data.session.SessionDAO
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

    protected fun decryptData(data: SymmetricResponse) = Cryptography().hybridDecrypt(
        privateKey, data.encrypted_aes_key, data.iv, data.ciphertext, data.tag
    )

    protected fun verifyData(response: DataResponse) =
        if (verifySignature(response.signature)) decryptData(response.encrypted_data) else ""

    protected inline fun <reified R> encryptData(data: R): SymmetricResponse? {
        return Cryptography().hybridEncrypt(publicKey, GsonBuilder().setDateFormat("yyyy-MM-dd").create().toJson(data))
    }
}
