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
import java.util.Calendar
import java.util.Locale

@HiltViewModel
class ImportSheetViewModel @Inject constructor(
    private val seasonRepository: SeasonRepository,
    private val importeRepository: ImporteRepository
) : ViewModel() {

    private val _importResult = MutableLiveData<AppResult<Int>?>()
    val importResult: LiveData<AppResult<Int>?> = _importResult

    private val _importing = MutableLiveData<Boolean>(false)
    val importing: LiveData<Boolean> = _importing

    private val _importProgress = MutableLiveData<Pair<Int, Int>>()
    val importProgress: LiveData<Pair<Int, Int>> = _importProgress

    fun onImportResultConsumed() {
        _importResult.value = null
    }

    fun importCsv(accountDetails: AccountDetails) {
        _importing.value = true
        viewModelScope.launch {
            try {
                data class ParsedTransaction(val transaction: Transaction, val date: Date, val yearMonth: Pair<Int, Int>)
                val parsed = accountDetails.transactions.mapNotNull { t ->
                    val date = parseDate(t.date) ?: return@mapNotNull null
                    val cal = Calendar.getInstance().apply { time = date }
                    val ym = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
                    ParsedTransaction(t, date, ym)
                }

                val seasonMap = mutableMapOf<Pair<Int, Int>, Season>()
                for (ym in parsed.map { it.yearMonth }.distinct()) {
                    val result = seasonRepository.getOrCreate(ym.first, ym.second)
                    if (result is AppResult.Error) {
                        _importResult.value = AppResult.Error(result.message, isControlled = true)
                        return@launch
                    }
                    seasonMap[ym] = (result as AppResult.Success).data!!
                }

                val total = parsed.size
                _importProgress.value = 0 to total
                var imported = 0
                for ((index, pt) in parsed.withIndex()) {
                    val amount = parseAmount(pt.transaction.amount)
                    val season = seasonMap[pt.yearMonth]
                    if (amount != null && season != null) {
                        val balanceAfter = parseAmount(pt.transaction.balanceAfter) ?: 0.0
                        val importe = Importe(
                            pt.transaction.concept,
                            pt.date,
                            amount,
                            balanceAfter,
                            season.getId()
                        )
                        val result = importeRepository.create(importe)
                        if (result is AppResult.Success) imported++
                    }
                    _importProgress.value = (index + 1) to total
                }

                _importResult.value = AppResult.Success("Imported $imported transactions", imported)
            } finally {
                _importing.value = false
            }
        }
    }

    private fun parseDate(dateStr: String): Date? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val parsed = sdf.parse(dateStr.trim()) ?: return null
            Date(parsed.time)
        } catch (e: Exception) {
            null
        }
    }

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
