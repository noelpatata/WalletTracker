package win.downops.wallettracker.di

import jakarta.inject.Inject
import jakarta.inject.Singleton
import win.downops.wallettracker.data.SeasonRepository
import win.downops.wallettracker.data.api.season.SeasonHttpService
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Season
import win.downops.wallettracker.data.sqlite.season.SeasonSqlService

@Singleton
class SeasonRepositoryProvider @Inject constructor(
    private val apiRepository: dagger.Lazy<SeasonHttpService>,
    private val localRepository: dagger.Lazy<SeasonSqlService>,
    private val appMode: AppMode
) : SeasonRepository {

    private fun repo(): SeasonRepository =
        if (appMode.isOnline) apiRepository.get() else localRepository.get()

    override suspend fun getAll(): AppResult<List<Season>> = repo().getAll()
    override suspend fun getById(seasonId: Long): AppResult<Season?> = repo().getById(seasonId)
    override suspend fun getByYearMonth(year: Int, month: Int): AppResult<Season?> = repo().getByYearMonth(year, month)
    override suspend fun getOrCreate(year: Int, month: Int): AppResult<Season?> = repo().getOrCreate(year, month)
    override suspend fun deleteById(seasonId: Long): AppResult<Unit> = repo().deleteById(seasonId)
}
