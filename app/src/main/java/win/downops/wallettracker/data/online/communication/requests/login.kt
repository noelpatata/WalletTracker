package win.downops.wallettracker.data.online.communication.requests

data class CipheredRequest(
    val encrypted_aes_key: String?,
    val iv: String?,
    val ciphertext: String?,
    val tag: String?
)
data class LoginRequest(
    val username: String,
    val password: String
)
data class AutoLoginRequest(
    val userId: Int,
    val ciphered: String
)
data class ServerPubKeyRequest(
    val publicKey: String
)