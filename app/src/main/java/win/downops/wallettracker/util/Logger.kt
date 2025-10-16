package win.downops.wallettracker.util

import android.util.Log
import win.downops.wallettracker.BuildConfig

object Logger {
    fun log(ex: Exception){
        if (BuildConfig.DEBUG) {
            Log.d(LogTag.DEBUG, ("${ex.message} \n ${ex.stackTrace.joinToString("\n")}"))
        }
    }
    fun log(message: String){
        if (BuildConfig.DEBUG) {
            Log.d(LogTag.DEBUG, (message))
        }
    }
}