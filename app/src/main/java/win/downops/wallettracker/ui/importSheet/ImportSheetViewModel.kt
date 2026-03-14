package win.downops.wallettracker.ui.importSheet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.ImporteRepository
import win.downops.wallettracker.data.SeasonRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Importe
import win.downops.wallettracker.data.models.Season
import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

@HiltViewModel
class ImportSheetViewModel @Inject constructor(
    private val seasonRepository: SeasonRepository,
    private val importeRepository: ImporteRepository
) : ViewModel() {

    private val _importResult = MutableLiveData<AppResult<Int>>()
    val importResult: LiveData<AppResult<Int>> = _importResult

    private val _importes = MutableLiveData<List<Importe>>()
    val importes: LiveData<List<Importe>> = _importes

    private val _seasons = MutableLiveData<List<Season>>()
    val seasons: LiveData<List<Season>> = _seasons

    fun loadSeasons() {
        viewModelScope.launch {
            val result = seasonRepository.getAll()
            if (result is AppResult.Success) {
                _seasons.value = result.data
            }
        }
    }

    fun loadImportesBySeason(seasonId: Long) {
        viewModelScope.launch {
            val result = importeRepository.getBySeasonId(seasonId)
            if (result is AppResult.Success) {
                _importes.value = result.data
            }
        }
    }

    fun importCsv(accountDetails: AccountDetails) {
        viewModelScope.launch {
            val (year, month) = parsePeriod(accountDetails.period)
                ?: run {
                    _importResult.value = AppResult.Error("Invalid period format", isControlled = true)
                    return@launch
                }

            val seasonResult = seasonRepository.getOrCreate(year, month)
            if (seasonResult is AppResult.Error) {
                _importResult.value = AppResult.Error(seasonResult.message, isControlled = true)
                return@launch
            }
            val season = (seasonResult as AppResult.Success).data!!

            var imported = 0
            for (transaction in accountDetails.transactions) {
                val date = parseDate(transaction.date) ?: continue
                val amount = parseAmount(transaction.amount) ?: continue
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
                val key = duplicateKey(transaction.concept, dateStr, amount)

                val balanceAfter = parseAmount(transaction.balanceAfter) ?: 0.0
                val importe = Importe(
                    transaction.concept,
                    date,
                    amount,
                    balanceAfter,
                    accountDetails.iban,
                    season.getId()
                )
                val result = importeRepository.create(importe)
                if (result is AppResult.Success) {
                    imported++
                }
            }

            val message = "Imported $imported transactions"
            _importResult.value = AppResult.Success(message, imported)
        }
    }

    private fun duplicateKey(concept: String, dateStr: String, amount: Double) =
        "$concept|$dateStr|$amount"

    // Period format: "09/03/2026 - 10/03/2026" → take start date
    private fun parsePeriod(period: String): Pair<Int, Int>? {
        return try {
            val startPart = period.split(" - ").first().trim()
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(startPart) ?: return null
            val cal = java.util.Calendar.getInstance().apply { time = date }
            Pair(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1)
        } catch (e: Exception) {
            null
        }
    }

    // Date format from CSV: "09/03/2026"
    private fun parseDate(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsed = sdf.parse(dateStr.trim()) ?: return null
            Date(parsed.time)
        } catch (e: Exception) {
            null
        }
    }

    // Amount format: "-1,70EUR" or "8.641,89EUR"
    private fun parseAmount(amountStr: String): Double? {
        return try {
            amountStr.trim()
                .replace("EUR", "")
                .replace(".", "")
                .replace(",", ".")
                .toDouble()
        } catch (e: Exception) {
            null
        }
    }
}
