import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import com.example.wallettracker.data.Session.SessionDAO
import java.io.File
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.crypto.Cipher

class Cryptography{
    private var context: Context? = null
    private var userId: Int? = null

    constructor(context: Context?, userId: Int) {
        this.context = context
        this.userId = userId
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun encrypt(text: String): String {
        SessionDAO(this.context).use { sSess ->
            val session = sSess.getByUserId(userId = this.userId!!)
            if (session != null && session.id > 0) {
                val privateKey =
                    loadPrivateKey(session.publicKey) //column missnamed (instead of publicKey is privateKey)
                val encryptedData = encryptWithPrivateKey(privateKey, text)
                return Base64.getEncoder().encodeToString(encryptedData)
            }

        }

        return ""

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeys(username: String) : ArrayList<String> {
        // Generate RSA key pair
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // Key size: 2048 bits
        val keyPair = keyPairGenerator.generateKeyPair()
        val privateKey = keyPair.private as RSAPrivateKey
        val publicKey = keyPair.public as RSAPublicKey

        val keys = ArrayList<String>()
        // Save private key to PEM file with username in the filename
        val privateKeyEncoded = savePrivateKey(privateKey, "private_key_$username.pem")

        // Save public key to PEM file with username in the filename
        val publicKeyEncoded = savePublicKey(publicKey, "public_key_$username.pem")

        keys.add(privateKeyEncoded)
        keys.add(publicKeyEncoded)

        return keys
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun savePrivateKey(privateKey: RSAPrivateKey, fileName: String) :String{
        val header = "-----BEGIN PRIVATE KEY-----\n"
        val footer = "\n-----END PRIVATE KEY-----"
        val encoded = privateKey.encoded
        val base64Encoded = Base64.getEncoder().encodeToString(encoded)
        return header + base64Encoded.chunked(64).joinToString("\n") + footer
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun savePublicKey(publicKey: RSAPublicKey, fileName: String) :String{
        val header = "-----BEGIN PUBLIC KEY-----\n"
        val footer = "\n-----END PUBLIC KEY-----"
        val encoded = publicKey.encoded
        val base64Encoded = Base64.getEncoder().encodeToString(encoded)
        return header + base64Encoded.chunked(64).joinToString("\n") + footer
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun encryptWithPrivateKey(privateKey: PrivateKey, data: String): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, privateKey)
        return cipher.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun readFile(fileName: String): String {
        return File(fileName).readText()
    }
}