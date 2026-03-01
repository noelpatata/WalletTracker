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

data class CategoryOption(val id: Long, val name: String)
data class MonthOption(val key: String, val label: String)

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

    private var allExpenses: List<Expense> = emptyList()
    private var categoryNameMap: Map<Long, String> = emptyMap()

    var selectedCategoryId: Long? = null
        private set
    var selectedMonthKey: String? = null
        private set

    private val _metricsResult = MutableLiveData<AppResult<MetricsData>>()
    val metricsResult: LiveData<AppResult<MetricsData>> = _metricsResult

    private val _categoryOptions = MutableLiveData<List<CategoryOption>>()
    val categoryOptions: LiveData<List<CategoryOption>> = _categoryOptions

    private val _monthOptions = MutableLiveData<List<MonthOption>>()
    val monthOptions: LiveData<List<MonthOption>> = _monthOptions

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val labelFormat = SimpleDateFormat("MMM yy", Locale.getDefault())

    fun loadMetrics() = viewModelScope.launch {
        selectedCategoryId = null
        selectedMonthKey = null

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

        val expensePairs = categories.map { cat ->
            async {
                val catId = cat.getId()
                when (val result = expenseRepo.getByCatId(catId)) {
                    is AppResult.Success -> catId to result.data
                    is AppResult.Error -> catId to emptyList<Expense>()
                }
            }
        }.awaitAll()

        categoryNameMap = categories.associate { it.getId() to it.getName() }
        allExpenses = expensePairs.flatMap { it.second }

        if (allExpenses.isEmpty()) {
            _metricsResult.postValue(AppResult.Error("No expenses found", isControlled = true))
            return@launch
        }

        val catOptions = categories
            .filter { cat -> expensePairs.any { it.first == cat.getId() && it.second.isNotEmpty() } }
            .map { CategoryOption(it.getId(), it.getName()) }
        _categoryOptions.postValue(catOptions)

        val monthOpts = allExpenses
            .mapNotNull { expense ->
                try { monthFormat.format(expense.getDate()) } catch (e: Exception) { null }
            }
            .distinct()
            .sortedDescending()
            .map { key ->
                val cal = Calendar.getInstance()
                try { cal.time = monthFormat.parse(key)!! } catch (e: Exception) {}
                MonthOption(key, labelFormat.format(cal.time))
            }
        _monthOptions.postValue(monthOpts)

        applyFilters()
    }

    fun setFilter(categoryId: Long?, monthKey: String?) {
        if (selectedCategoryId == categoryId && selectedMonthKey == monthKey) return
        selectedCategoryId = categoryId
        selectedMonthKey = monthKey
        applyFilters()
    }

    private fun applyFilters() {
        if (allExpenses.isEmpty()) return

        var filtered = allExpenses
        selectedCategoryId?.let { catId ->
            filtered = filtered.filter { it.getCategoryId() == catId }
        }
        selectedMonthKey?.let { monthKey ->
            filtered = filtered.filter { expense ->
                try { monthFormat.format(expense.getDate()) == monthKey } catch (e: Exception) { false }
            }
        }

        if (filtered.isEmpty()) {
            _metricsResult.postValue(AppResult.Error("No expenses for the selected filters", isControlled = true))
            return
        }

        val total = filtered.sumOf { try { it.getPrice() } catch (e: Exception) { 0.0 } }
        val count = filtered.size
        val average = if (count > 0) total / count else 0.0

        val categoryMetrics = filtered
            .groupBy { it.getCategoryId() }
            .map { (catId, expenses) ->
                val catTotal = expenses.sumOf { try { it.getPrice() } catch (e: Exception) { 0.0 } }
                CategoryMetric(
                    name = categoryNameMap[catId] ?: "Unknown",
                    total = catTotal,
                    percentage = if (total > 0) (catTotal / total) * 100 else 0.0
                )
            }
            .sortedByDescending { it.total }

        val monthMetrics = buildMonthMetrics(filtered)

        _metricsResult.postValue(
            AppResult.Success("Metrics loaded", MetricsData(total, count, average, categoryMetrics, monthMetrics))
        )
    }

    private fun buildMonthMetrics(expenses: List<Expense>): List<MonthMetric> {
        val totalsPerMonth = expenses
            .groupBy { try { monthFormat.format(it.getDate()) } catch (e: Exception) { "" } }
            .filterKeys { it.isNotEmpty() }
            .mapValues { (_, list) -> list.sumOf { try { it.getPrice() } catch (e: Exception) { 0.0 } } }

        val maxTotal = totalsPerMonth.values.maxOrNull() ?: 1.0

        return totalsPerMonth.entries
            .sortedBy { it.key }
            .map { (key, monthTotal) ->
                val cal = Calendar.getInstance()
                try { cal.time = monthFormat.parse(key)!! } catch (e: Exception) {}
                MonthMetric(
                    label = labelFormat.format(cal.time),
                    total = monthTotal,
                    percentage = if (maxTotal > 0) (monthTotal / maxTotal) * 100 else 0.0
                )
            }
    }
}
