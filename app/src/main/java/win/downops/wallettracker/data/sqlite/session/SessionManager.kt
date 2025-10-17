package win.downops.wallettracker.data.sqlite.session

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
class SessionManager(private val context: Context) {

    suspend fun isOnline(): Boolean = withContext(Dispatchers.IO) {
        val session = SessionService(context).getFirstSession()
        session?.online ?: true
    }
}