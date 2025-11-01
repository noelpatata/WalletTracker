package win.downops.wallettracker.di

import win.downops.wallettracker.data.sqlite.SessionRepository
import win.downops.wallettracker.data.sqlite.session.SessionService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryProvider @Inject constructor(
    private val repository: SessionService
){
    fun get(): SessionRepository {
        return repository
    }
}