package win.downops.wallettracker.data.session

class Session {
    var id: Int = 0
    var token: String = ""
    var privateKey: String = ""
    var serverPublicKey: String = ""
    var remember: Boolean = false
    var online: Boolean = false
}