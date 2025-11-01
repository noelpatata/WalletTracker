package win.downops.wallettracker.data.models

class Session {
    var id: Int = 0
    var token: String = ""
    var privateKey: String = ""
    var serverPublicKey: String = ""
    var cipheredCredentials: String = ""
    var iv: String = ""
    var fingerPrint: Boolean = false
}