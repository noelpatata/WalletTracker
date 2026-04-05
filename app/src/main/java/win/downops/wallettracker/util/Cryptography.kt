package win.downops.wallettracker.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.annotation.RequiresApi
import win.downops.wallettracker.BuildConfig
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

object Cryptography {
    private const val CLIENT_KEY_ALIAS = "client_key"
    private const val ANDROID_KEY_STORE = "AndroidKeyStore"

    fun getOrCreateClientKeyPair(): KeyPair {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        if (keyStore.containsAlias(CLIENT_KEY_ALIAS)) {
            val privateKey = keyStore.getKey(CLIENT_KEY_ALIAS, null) as PrivateKey
            val publicKey = keyStore.getCertificate(CLIENT_KEY_ALIAS).publicKey
            return KeyPair(publicKey, privateKey)
        }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA,
            ANDROID_KEY_STORE
        )
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(
                CLIENT_KEY_ALIAS,
                KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_DECRYPT
            )
            .setKeySize(2048)
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            .build()
        )
        return keyPairGenerator.generateKeyPair()
    }

    fun getClientPublicKeyPem(): String {
        val keyPair = getOrCreateClientKeyPair()
        return getPublicKeyPem(keyPair.public as RSAPublicKey)
    }

    fun getPublicKeyPem(publicKey: RSAPublicKey): String {
        val encoded = publicKey.encoded
        val pem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.encodeToString(encoded, Base64.DEFAULT).chunked(64).joinToString("\n") + "\n" +
                "-----END PUBLIC KEY-----"
        return Base64.encodeToString(pem.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    fun signWithKeystore(data: ByteArray): String {
        val keyPair = getOrCreateClientKeyPair()
        val signature = Signature.getInstance("SHA256withRSA/PSS")
        signature.initSign(keyPair.private)
        signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))
        signature.update(data)
        val signBytes = signature.sign()
        return Base64.encodeToString(signBytes, Base64.NO_WRAP)
    }

    fun decryptWithKeystore(
        encryptedAesKeyBase64: String,
        ivBase64: String,
        cipherTextBase64: String,
        tagBase64: String
    ): String {
        return try {
            val keyPair = getOrCreateClientKeyPair()
            val encryptedAesKey = Base64.decode(encryptedAesKeyBase64, Base64.DEFAULT)
            val iv = Base64.decode(ivBase64, Base64.DEFAULT)
            val cipherText = Base64.decode(cipherTextBase64, Base64.DEFAULT)
            val tag = Base64.decode(tagBase64, Base64.DEFAULT)

            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.DECRYPT_MODE, keyPair.private)
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

    fun verify(publicKeyB64: String, signatureB64: String): Boolean {
        return try {
            val publicKey = loadPublicKey(publicKeyB64)
            val signatureBytes = Base64.decode(signatureB64, Base64.DEFAULT)
            val signature = Signature.getInstance("SHA256withRSA/PSS")
            signature.initVerify(publicKey)
            signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))
            signature.update(BuildConfig.SIGN_SECRET.toByteArray())
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

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
            
            CipheredRequest(
                Base64.encodeToString(encryptedAesKey, Base64.NO_WRAP),
                Base64.encodeToString(iv, Base64.NO_WRAP),
                Base64.encodeToString(cipherText, Base64.NO_WRAP),
                Base64.encodeToString(tag, Base64.NO_WRAP)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadPublicKey(publicKeyEncoded: String): RSAPublicKey {
        val publicKeyDecodedBytes = Base64.decode(publicKeyEncoded, Base64.DEFAULT)
        val publicKeyDecoded = String(publicKeyDecodedBytes)
        val base64String = publicKeyDecoded
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(decodedBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }

    fun isTokenValid(jwt: String): Boolean {
        return try {
            val payload = jwt.split(".").getOrNull(1) ?: return false
            val decoded = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP))
            val exp = Regex("\"exp\":(\\d+)").find(decoded)?.groupValues?.get(1)?.toLong() ?: return false
            System.currentTimeMillis() / 1000 < exp
        } catch (e: Exception) {
            false
        }
    }
}
