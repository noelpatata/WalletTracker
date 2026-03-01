package win.downops.wallettracker.data.api.communication.responses

data class LoginResponse(
    val token: String
)
data class ServerPubKeyResponse(
    val userId: Int,
    val publicKey: String
)