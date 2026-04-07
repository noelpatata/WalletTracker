package win.downops.wallettracker.util

import android.os.Build
import win.downops.wallettracker.BuildConfig
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.communication.requests.CipheredRequest
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import java.util.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

object Cryptography {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val CLIENT_KEY_ALIAS = "win.downops.wallettracker.CLIENT_KEY"

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateAndStoreKeys(): String {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE
        )

        val parameterSpec = KeyGenParameterSpec.Builder(
            CLIENT_KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_DECRYPT
        ).run {
            setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA1)  // Add SHA1 for OAEP
            setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PSS)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
            setKeySize(2048)
            build()
        }

        keyPairGenerator.initialize(parameterSpec)
        val keyPair = keyPairGenerator.generateKeyPair()
        return getPublicKeyB64(keyPair.public as RSAPublicKey)
    }

    private fun getPrivateKeyFromStore(): PrivateKey? {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        return keyStore.getKey(CLIENT_KEY_ALIAS, null) as? PrivateKey
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeys(): List<String> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        val privateKeyEncoded = getPrivateKeyB64(keyPair.private as RSAPrivateKey)
        val publicKeyEncoded = getPublicKeyB64(keyPair.public as RSAPublicKey)

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
    fun sign(): String {
        val privateKey = getPrivateKeyFromStore() ?: throw IllegalStateException("Key not found")

        val signature = Signature.getInstance("SHA256withRSA/PSS")
        signature.initSign(privateKey)
        // ❌ REMOVE THIS LINE - Keystore doesn't support setParameter()
        // signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))

        signature.update(BuildConfig.SIGN_SECRET.toByteArray())
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(publicKeyB64: String, signatureB64: String): Boolean {
        return try {
            val publicKey = loadPublicKey(publicKeyB64)
            val signatureBytes = Base64.getDecoder().decode(signatureB64)
            val signature = Signature.getInstance("SHA256withRSA/PSS")
            signature.initVerify(publicKey)
            // ❌ REMOVE THIS LINE
            // signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))

            signature.update(BuildConfig.SIGN_SECRET.toByteArray())
            signature.verify(signatureBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun hybridDecrypt(
        encryptedAesKeyBase64: String,
        ivBase64: String,
        cipherTextBase64: String,
        tagBase64: String
    ): String {
        return try {
            val privateKey = getPrivateKeyFromStore() ?: throw IllegalStateException("Key not found")

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
            val iv = ByteArray(12)
            SecureRandom().nextBytes(iv)

            val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(aesKey, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)

            val cipherTextWithTag = aesCipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            val cipherText = cipherTextWithTag.copyOfRange(0, cipherTextWithTag.size - 16)
            val tag = cipherTextWithTag.copyOfRange(cipherTextWithTag.size - 16, cipherTextWithTag.size)

            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encryptedAesKey = rsaCipher.doFinal(aesKey)

            CipheredRequest(
                Base64.getEncoder().encodeToString(encryptedAesKey),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(cipherText),
                Base64.getEncoder().encodeToString(tag)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun isTokenValid(jwt: String): Boolean {
        return try {
            val payload = jwt.split(".").getOrNull(1) ?: return false
            val padded = payload.padEnd((payload.length + 3) / 4 * 4, '=')
            val decoded = String(Base64.getDecoder().decode(padded))
            val exp = Regex("\"exp\":(\\d+)").find(decoded)?.groupValues?.get(1)?.toLong() ?: return false
            System.currentTimeMillis() / 1000 < exp
        } catch (e: Exception) {
            false
        }
    }

    fun deleteKeys() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.deleteEntry(CLIENT_KEY_ALIAS)
    }
}
