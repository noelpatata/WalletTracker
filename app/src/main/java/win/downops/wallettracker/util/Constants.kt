 package win.downops.wallettracker.util

object Secrets {
    const val apiUrl = "https://192.168.0.21"
}
object Messages {
    const val cantConnect = "Cannot connect to server"
    const val loginFailedMessage = "Login failed"
    const val networkErrorMessage = "Network error"
    const val authenticationErrorMessage = "Security error during data fetching"
    const val noDataMessage = "No data to show"
    const val invalidData = "Data received is invalid"
    const val invalidSignature = "Error verifying signature"
    const val errorFetchingPublicKey = "Error fetching server's public key"
    const val errorSendingPublicKey = "Error sending public key to server"
    const val fetchingDataMessage = "Error fetching data"
    const val errorCreatingData = "Error creating data"
    const val errorUpdatingData = "Error updating data"
    const val errorDeletingData = "Error deleting data"
}
 object LogTag{
     const val DEBUG = "Debug"
 }