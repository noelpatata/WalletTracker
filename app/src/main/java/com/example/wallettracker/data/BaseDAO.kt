import Cryptography
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.SymmetricResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.session.SessionDAO
import com.example.wallettracker.util.Constantes.authenticationErrorMessage

@RequiresApi(Build.VERSION_CODES.O)
abstract class BaseDAO<T>(private val context: Context) {
    var cipheredText: String = ""
    var privateKey: String = ""
    var publicKey: String = ""
    var token: String = ""
    var userId: Int = 0

    init {
        SessionDAO(context).use { sSess ->
            val sess = sSess.getFirstSession()
            if (sess != null) {
                cipheredText = Cryptography().sign(sess.privateKey)
                token = sess.token
                userId = sess.userId
                privateKey = sess.privateKey
                publicKey = sess.serverPublicKey
            }
            else{
                throw Exception("No session found")
            }
        }
    }



    fun verifySignature(signatureB64: String): Boolean {
        return Cryptography().verify(publicKey, signatureB64)
    }

    fun decryptData(data: SymmetricResponse): String{
        return Cryptography().hybridDecrypt(privateKey, data.encrypted_aes_key, data.iv, data.ciphertext, data.tag)
    }

    fun verifyData(data: DataResponse): String{
        val signed = verifySignature(data.signature)
        if (signed){
            val jsonData = decryptData(data.encrypted_data)
            if(jsonData != ""){ return jsonData }

        }
        return ""
    }
}