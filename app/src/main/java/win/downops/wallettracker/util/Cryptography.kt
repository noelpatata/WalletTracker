import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.MGF1ParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.PSSParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import java.util.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class Cryptography {
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeys(): List<String> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        val privateKey = keyPair.private
        val publicKey = keyPair.public

        val privateKeyEncoded = getPrivateKeyB64(privateKey as RSAPrivateKey)
        val publicKeyEncoded = getPublicKeyB64(publicKey as RSAPublicKey)

        return listOf(privateKeyEncoded, publicKeyEncoded)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPrivateKeyB64(privateKey: RSAPrivateKey): String {
        val encoded = privateKey.encoded

        val pem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") + "\n" +
                "-----END PRIVATE KEY-----"

        return Base64.getEncoder().encodeToString(pem.toByteArray(Charsets.UTF_8))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPublicKeyB64(publicKey: RSAPublicKey): String {
        val encoded = publicKey.encoded

        val pem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") + "\n" +
                "-----END PUBLIC KEY-----"

        return Base64.getEncoder().encodeToString(pem.toByteArray(Charsets.UTF_8))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sign(privateKeyBase64: String): String {
        val privateKey = loadPrivateKey(privateKeyBase64)

        val signature = Signature.getInstance("SHA256withRSA/PSS")
        signature.initSign(privateKey)
        signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))
        signature.update("s0m3r4nd0mt3xt".toByteArray())
        val signBytes = signature.sign()

        return Base64.getEncoder().encodeToString(signBytes)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(publicKeyB64: String, signatureB64: String): Boolean {
        return try {
            val publicKey = loadPublicKey(publicKeyB64)

            val signatureBytes = Base64.getDecoder().decode(signatureB64)

            val signature = Signature.getInstance("SHA256withRSA/PSS")
            signature.initVerify(publicKey)

            signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))

            signature.update("s0m3r4nd0mt3xt".toByteArray())

            signature.verify(signatureBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hybridDecrypt(
        privateKeyBase64: String,
        encryptedAesKeyBase64: String,
        ivBase64: String,
        cipherTextBase64: String,
        tagBase64: String
    ): String {
        return try {
            val privateKey = loadPrivateKey(privateKeyBase64)

            val encryptedAesKey = Base64.getDecoder().decode(encryptedAesKeyBase64)
            val iv = Base64.getDecoder().decode(ivBase64)
            val cipherText = Base64.getDecoder().decode(cipherTextBase64)
            val tag = Base64.getDecoder().decode(tagBase64)

            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val aesKey = rsaCipher.doFinal(encryptedAesKey)

            val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(aesKey, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            val fullCipherText = cipherText + tag
            val decryptedBytes = aesCipher.doFinal(fullCipherText)

            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun hybridEncrypt(publicKeyBase64: String, plaintext: String): CipheredRequest? {
        return try {
            val publicKey = loadPublicKey(publicKeyBase64)

            val aesKey = ByteArray(32)
            SecureRandom().nextBytes(aesKey)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            val secretKey = SecretKeySpec(aesKey, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            val cipherTextWithTag = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

            val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - 16)
            val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - 16, cipherTextWithTag.size)

            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedAesKey = rsaCipher.doFinal(aesKey)
            val encryptedObject = CipheredRequest(
                Base64.getEncoder().encodeToString(encryptedAesKey),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(tag)
            )
            return encryptedObject
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPrivateKey(privateKeyEncoded: String): RSAPrivateKey {
        val privateKeyDecodedBytes = Base64.getDecoder().decode(privateKeyEncoded)
        val privateKeyDecoded = String(privateKeyDecodedBytes)
        val base64String = privateKeyDecoded
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = Base64.getDecoder().decode(base64String)
        val keySpec = PKCS8EncodedKeySpec(decodedBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec) as RSAPrivateKey
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPublicKey(publicKeyEncoded: String): RSAPublicKey {
        val publicKeyDecodedBytes = Base64.getDecoder().decode(publicKeyEncoded)
        val publicKeyDecoded = String(publicKeyDecodedBytes)
        val base64String = publicKeyDecoded
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = Base64.getDecoder().decode(base64String)
        val keySpec = X509EncodedKeySpec(decodedBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }
}
