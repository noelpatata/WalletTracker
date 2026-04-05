package win.downops.wallettracker.data.models

class Session {
    var token: String = ""
    var username: String = ""
    var serverPublicKey: String = ""
    var encryptedCredentials: String = "" // Encrypted with server public key (CipheredRequest JSON)
    var biometricsCredentials: String = "" // Encrypted locally for fingerprint
    var biometricsIv: String = ""
    var fingerPrint: Boolean = false
    var online: Boolean = false
}
