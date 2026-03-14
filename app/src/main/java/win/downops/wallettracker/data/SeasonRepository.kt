package win.downops.wallettracker.data

import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Season

interface SeasonRepository {
    suspend fun getAll(): AppResult<List<Season>>
    suspend fun getById(seasonId: Long): AppResult<Season?>
    suspend fun getByYearMonth(year: Int, month: Int): AppResult<Season?>
    suspend fun getOrCreate(year: Int, month: Int): AppResult<Season?>
    suspend fun deleteById(seasonId: Long): AppResult<Unit>
}
