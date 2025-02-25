import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import com.example.wallettracker.data.session.SessionDAO
import java.io.File
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.crypto.Cipher

class Cryptography(private var context: Context?, userId: Int) {
    private var userId: Int? = userId
    private val randomText = "s0m3r4nd0mt3xt"

    @RequiresApi(Build.VERSION_CODES.O)
    fun sign(): String {
        SessionDAO(this.context).use { sSess ->
            val session = sSess.getByUserId(userId = this.userId!!)
            if (session.id > 0) {
                val privateKey =
                    loadPrivateKey(session.privateKey)
                val signedData = signWithPrivateKey(privateKey, randomText)
                return Base64.getEncoder().encodeToString(signedData)
            }
        }

        return ""
    }
    fun signWithPrivateKey(privateKey: PrivateKey, text: String): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(text.toByteArray())
        return signature.sign()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeys(): ArrayList<String> {
        // Generate RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // Key size: 2048 bits
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private as RSAPrivateKey
        val publicKey = keyPair.public as RSAPublicKey

        val keys = ArrayList<String>()
        // Base64 encode private and public keys without the PEM formatting
        val privateKeyEncoded = getPrivateKeyB64(privateKey)
        val publicKeyEncoded = getPublicKeyB64(publicKey)

        keys.add(privateKeyEncoded)
        keys.add(publicKeyEncoded)

        return keys
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPrivateKeyB64(privateKey: RSAPrivateKey): String {
        val encoded = privateKey.encoded
        return Base64.getEncoder().encodeToString(encoded) // Return Base64 encoded string
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPublicKeyB64(publicKey: RSAPublicKey): String {
        val encoded = publicKey.encoded
        return Base64.getEncoder().encodeToString(encoded) // Return Base64 encoded string
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPrivateKey(privateKeyEncoded: String): PrivateKey {
        val pemContent = privateKeyEncoded
        val base64String = pemContent
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = Base64.getDecoder().decode(base64String)
        return java.security.KeyFactory.getInstance("RSA")
            .generatePrivate(java.security.spec.PKCS8EncodedKeySpec(decodedBytes))
    }

}