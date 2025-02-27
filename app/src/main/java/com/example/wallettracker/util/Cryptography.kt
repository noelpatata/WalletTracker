import android.os.Build
import androidx.annotation.RequiresApi
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

    /**
     * Generates an RSA key pair (2048 bits) with PKCS#8 format for private key and X.509 format for public key.
     * The keys are returned as Base64 encoded strings.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun generateKeys(): List<String> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048) // RSA key size: 2048 bits
        val keyPair = keyPairGenerator.generateKeyPair()

        val privateKey = keyPair.private
        val publicKey = keyPair.public

        // Return Base64 encoded keys in the appropriate format
        val privateKeyEncoded = getPrivateKeyB64(privateKey as RSAPrivateKey)
        val publicKeyEncoded = getPublicKeyB64(publicKey as RSAPublicKey)

        return listOf(privateKeyEncoded, publicKeyEncoded)
    }

    /**
     * Convert private key to Base64 (PKCS#8 format)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPrivateKeyB64(privateKey: RSAPrivateKey): String {
        val encoded = privateKey.encoded

        // Create PEM format
        val pem = "-----BEGIN PRIVATE KEY-----\n" +
                Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") + "\n" +
                "-----END PRIVATE KEY-----"

        return Base64.getEncoder().encodeToString(pem.toByteArray(Charsets.UTF_8))
    }

    /**
     * Convert public key to Base64 (X.509 format)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getPublicKeyB64(publicKey: RSAPublicKey): String {
        val encoded = publicKey.encoded

        // Create PEM format
        val pem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(encoded).chunked(64).joinToString("\n") + "\n" +
                "-----END PUBLIC KEY-----"

        return Base64.getEncoder().encodeToString(pem.toByteArray(Charsets.UTF_8))
    }

    /**
     * Signs data using the private key with RSA/PSS and SHA256.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sign(privateKeyBase64: String): String {
        val privateKey = loadPrivateKey(privateKeyBase64)

        val signature = Signature.getInstance("SHA256withRSA/PSS")
        signature.initSign(privateKey)
        signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))
        signature.update("s0m3r4nd0mt3xt".toByteArray())
        val signBytes = signature.sign()

        // Return the Base64 encoded signature
        return Base64.getEncoder().encodeToString(signBytes)
    }

    /**
     * Verifies a signature using the public key with X.509 format.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun verify(publicKeyB64: String, signatureB64: String): Boolean {
        return try {
            val publicKey = loadPublicKey(publicKeyB64)

            // Decode Base64 signature
            val signatureBytes = Base64.getDecoder().decode(signatureB64)

            // Initialize Signature instance
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

    /**
     * Decrypts the data using the private key with RSA/ECB/OAEPWithSHA-256AndMGF1Padding.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun decrypt(privateKeyBase64: String, cipherTextBase64: String): String {
        return try {
            val privateKey = loadPrivateKey(privateKeyBase64)

            // Decode Base64 cipher text
            val cipherTextBytes = Base64.getDecoder().decode(cipherTextBase64)

            // Initialize Cipher for RSA decryption
            val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            // Perform decryption
            val decryptedBytes = cipher.doFinal(cipherTextBytes)

            // Convert decrypted bytes to string (assuming it's a JSON string)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "" // Return empty JSON string in case of failure
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

            // Decode Base64 values
            val encryptedAesKey = Base64.getDecoder().decode(encryptedAesKeyBase64)
            val iv = Base64.getDecoder().decode(ivBase64)
            val cipherText = Base64.getDecoder().decode(cipherTextBase64)
            val tag = Base64.getDecoder().decode(tagBase64)

            // 🔹 Step 1: Decrypt AES key using RSA
            val rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)
            val aesKey = rsaCipher.doFinal(encryptedAesKey)

            // 🔹 Step 2: Decrypt data using AES-GCM
            val aesCipher = Cipher.getInstance("AES/GCM/NoPadding")
            val secretKey = SecretKeySpec(aesKey, "AES")
            val gcmSpec = GCMParameterSpec(128, iv)
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)

            // 🔹 Append authentication tag to ciphertext
            val fullCipherText = cipherText + tag
            val decryptedBytes = aesCipher.doFinal(fullCipherText)

            // Convert decrypted bytes to string
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }


    /**
     * Load Private Key from Base64 encoded string (PKCS#8 format)
     */
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

    /**
     * Load Public Key from Base64 encoded string (X.509 format)
     */
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
