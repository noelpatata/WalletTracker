package win.downops.wallettracker.data.models

sealed class AppResult<out T> {
    data class Success<out T>(val message: String, val data: T) : AppResult<T>()
    data class Error(val message: String, val isControlled: Boolean = true, val stackTrace: String? = null) : AppResult<Nothing>()
}