package win.downops.wallettracker.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.util.Log
import android.widget.Toast
import win.downops.wallettracker.BuildConfig
import win.downops.wallettracker.MainActivity
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.ui.login.LoginActivity
import win.downops.wallettracker.util.Messages.unexpectedError

object AppResultHandler {
    fun handleError(context: Context, result: AppResult.Error) {
        if (BuildConfig.DEBUG) {
            Log.d(LogTag.DEBUG, "AppResult.Error [code=${result.code}]: ${result.message}\n${result.stackTrace}")
        }

        if (result.code == 401) {
            val activity = findActivity(context)
            if (activity is MainActivity) {
                activity.doLogOut()
            } else if (activity is LoginActivity) {
                // Already in login activity
            } else {
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            }
            Toast.makeText(context, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val message = if (result.isControlled) {
            result.message
        } else {
            unexpectedError
        }

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun findActivity(context: Context?): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }
}
