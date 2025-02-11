import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.Base64
import javax.crypto.Cipher

@RequiresApi(Build.VERSION_CODES.O)
fun encrypt(text: String): String{
    val privateKey = loadPrivateKey("private_key.pem")

    val encryptedData = encryptWithPrivateKey(privateKey, text)
    return Base64.getEncoder().encodeToString(encryptedData)
}

@RequiresApi(Build.VERSION_CODES.O)
fun generateKeys(username: String) {
    // Generate RSA key pair
    val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyPairGenerator.initialize(2048) // Key size: 2048 bits
    val keyPair = keyPairGenerator.generateKeyPair()
    val privateKey = keyPair.private as RSAPrivateKey
    val publicKey = keyPair.public as RSAPublicKey

    // Save private key to PEM file with username in the filename
    savePrivateKey(privateKey, "private_key_$username.pem")

    // Save public key to PEM file with username in the filename
    savePublicKey(publicKey, "public_key_$username.pem")

    val pblic = readFile("public_key_$username.pem")
    val prblic = readFile("private_key_$username.pem")
    val a = pblic

    println("Keys generated successfully for user '$username':")
    println("- private_key_$username.pem")
    println("- public_key_$username.pem")
}

@RequiresApi(Build.VERSION_CODES.O)
fun savePrivateKey(privateKey: RSAPrivateKey, fileName: String) {
    val header = "-----BEGIN PRIVATE KEY-----\n"
    val footer = "\n-----END PRIVATE KEY-----"
    val encoded = privateKey.encoded
    val base64Encoded = Base64.getEncoder().encodeToString(encoded)
    File(fileName).writeText(header + base64Encoded.chunked(64).joinToString("\n") + footer)
}

@RequiresApi(Build.VERSION_CODES.O)
fun savePublicKey(publicKey: RSAPublicKey, fileName: String) {
    val header = "-----BEGIN PUBLIC KEY-----\n"
    val footer = "\n-----END PUBLIC KEY-----"
    val encoded = publicKey.encoded
    val base64Encoded = Base64.getEncoder().encodeToString(encoded)
    File(fileName).writeText(header + base64Encoded.chunked(64).joinToString("\n") + footer)
}

@RequiresApi(Build.VERSION_CODES.O)
fun loadPrivateKey(fileName: String): PrivateKey {
    val pemContent = File(fileName).readText()
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