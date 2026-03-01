package win.downops.wallettracker.data

import win.downops.wallettracker.data.models.Session

interface SessionRepository {
    fun edit(session: Session): Int
    fun insert(session: Session): Long
    fun deleteAll()
    fun getFirstSession(): Session?
    fun close()
}