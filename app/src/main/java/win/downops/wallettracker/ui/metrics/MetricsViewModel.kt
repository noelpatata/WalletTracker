package win.downops.wallettracker.ui.metrics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CategoryMetric(
    val name: String,
    val total: Double,
    val percentage: Double
)

data class MonthMetric(
    val label: String,
    val total: Double,
    val percentage: Double
)

data class MetricsData(
    val total: Double,
    val count: Int,
    val average: Double,
    val categories: List<CategoryMetric>,
    val months: List<MonthMetric>
)

@HiltViewModel
class MetricsViewModel @Inject constructor(
    private val categoryRepo: ExpenseCategoryRepository,
    private val expenseRepo: ExpenseRepository
) : ViewModel() {

    private val _metricsResult = MutableLiveData<AppResult<MetricsData>>()
    val metricsResult: LiveData<AppResult<MetricsData>> = _metricsResult

    fun loadMetrics() = viewModelScope.launch {
        val categoriesResult = categoryRepo.getAll()
        if (categoriesResult is AppResult.Error) {
            _metricsResult.postValue(AppResult.Error(categoriesResult.message, categoriesResult.isControlled))
            return@launch
        }

        val categories = (categoriesResult as AppResult.Success).data
        if (categories.isEmpty()) {
            _metricsResult.postValue(AppResult.Error("No expenses found", isControlled = true))
            return@launch
        }

        val expensesByCategory = categories.map { cat ->
            async {
                when (val result = expenseRepo.getByCatId(cat.getId())) {
                    is AppResult.Success -> result.data
                    is AppResult.Error -> emptyList()
                }
            }
        }.awaitAll()

        val allExpenses = expensesByCategory.flatten()
        val total = categories.sumOf { it.getTotal() }
        val count = allExpenses.size
        val average = if (count > 0) total / count else 0.0

        val categoryMetrics = categories
            .filter { it.getTotal() > 0 }
            .sortedByDescending { it.getTotal() }
            .map { cat ->
                CategoryMetric(
                    name = cat.getName(),
                    total = cat.getTotal(),
                    percentage = if (total > 0) (cat.getTotal() / total) * 100 else 0.0
                )
            }

        val monthMetrics = buildMonthMetrics(allExpenses)

        _metricsResult.postValue(
            AppResult.Success("Metrics loaded", MetricsData(total, count, average, categoryMetrics, monthMetrics))
        )
    }

    private fun buildMonthMetrics(expenses: List<Expense>): List<MonthMetric> {
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val labelFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

        val last6Months = (0 until 6).map { offset ->
            Calendar.getInstance().apply {
                add(Calendar.MONTH, -offset)
                set(Calendar.DAY_OF_MONTH, 1)
            }
        }.reversed()

        val totalsPerMonth = expenses.groupBy { expense ->
            try { monthFormat.format(expense.getDate()) } catch (e: Exception) { "" }
        }.filterKeys { it.isNotEmpty() }
            .mapValues { (_, list) -> list.sumOf { try { it.getPrice() } catch (e: Exception) { 0.0 } } }

        val result = last6Months.map { cal ->
            val key = monthFormat.format(cal.time)
            val label = labelFormat.format(cal.time)
            val monthTotal = totalsPerMonth[key] ?: 0.0
            key to Pair(label, monthTotal)
        }

        val maxMonthTotal = result.maxOfOrNull { it.second.second } ?: 1.0

        return result.map { (_, pair) ->
            val (label, monthTotal) = pair
            MonthMetric(
                label = label,
                total = monthTotal,
                percentage = if (maxMonthTotal > 0) (monthTotal / maxMonthTotal) * 100 else 0.0
            )
        }
    }
}
