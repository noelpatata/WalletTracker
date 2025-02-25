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

        return pem
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

        return pem
    }

    /**
     * Signs data using the private key with RSA/PSS and SHA256.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun sign(privateKeyBase64: String): String {
        // Load the private key from the Base64 encoded string
        val privateKey = loadPrivateKey(privateKeyBase64)

        // Initialize the Signature object with SHA256withRSA/PSS
        val signature = Signature.getInstance("SHA256withRSA/PSS")
        signature.initSign(privateKey)

        // Set the PSS parameters (MGF1, Salt length, and trailer field)
        signature.setParameter(PSSParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, 32, 1))

        // Update the signature with the data to be signed
        signature.update("s0m3r4nd0mt3xt".toByteArray())

        // Generate the signed data
        val signBytes = signature.sign()

        // Return the Base64 encoded signature
        return Base64.getEncoder().encodeToString(signBytes)
    }

    /**
     * Load Private Key from Base64 encoded string (PKCS#8 format)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun loadPrivateKey(privateKeyEncoded: String): RSAPrivateKey {
        val base64String = privateKeyEncoded
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
        val base64String = publicKeyEncoded
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replace("\\s+".toRegex(), "")

        val decodedBytes = Base64.getDecoder().decode(base64String)
        val keySpec = X509EncodedKeySpec(decodedBytes)
        return KeyFactory.getInstance("RSA").generatePublic(keySpec) as RSAPublicKey
    }
}
