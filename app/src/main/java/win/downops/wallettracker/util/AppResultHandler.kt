package win.downops.wallettracker.util

import android.content.Context
import android.util.Log
import android.widget.Toast
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.util.Messages.unexpectedError

object AppResultHandler {
    fun handleError(context: Context, result: AppResult.Error) {
        if (BuildConfig.DEBUG) {
            Log.d(LogTag.DEBUG, "${result.message}\n${result.stackTrace}")
        }

        val message = if (result.isControlled) {
            result.message
        } else {
            unexpectedError
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}