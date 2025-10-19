package win.downops.wallettracker.ui.createExpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Build
import androidx.annotation.RequiresApi
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.di.ExpenseCategoryRepositoryProvider
import win.downops.wallettracker.di.ExpenseRepositoryProvider

class CreateExpenseViewModel @Inject constructor(
    private val expenseRepositoryProvider: ExpenseRepositoryProvider,
    private val categoryRepositoryProvider: ExpenseCategoryRepositoryProvider
) : ViewModel() {

    private val _getExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val getExpenseResult: LiveData<AppResult<Expense?>> = _getExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpense(id: Long) = viewModelScope.launch {
        val repo = expenseRepositoryProvider.get()
        _getExpenseResult.postValue(repo.getById(id))
    }

    private val _createExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val createExpenseResult: LiveData<AppResult<Expense?>> = _createExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun createExpense(expense: Expense) = viewModelScope.launch {
        val repo = expenseRepositoryProvider.get()
        _createExpenseResult.postValue(repo.create(expense))
    }

    private val _editExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val editExpenseResult: LiveData<AppResult<Expense?>> = _editExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun editExpense(expense: Expense) = viewModelScope.launch {
        val repo = expenseRepositoryProvider.get()
        _editExpenseResult.postValue(repo.edit(expense))
    }

    private val _deleteExpenseResult = MutableLiveData<AppResult<Unit>>()
    val deleteExpenseResult: LiveData<AppResult<Unit>> = _deleteExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExpense(id: Long) = viewModelScope.launch {
        val repo = expenseRepositoryProvider.get()
        _deleteExpenseResult.postValue(repo.deleteById(id))
    }

    private val _getCategoriesResult = MutableLiveData<AppResult<List<ExpenseCategory>>>()
    val getCategoriesResult: LiveData<AppResult<List<ExpenseCategory>>> = _getCategoriesResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun getCategories() = viewModelScope.launch {
        val repo = categoryRepositoryProvider.get()
        _getCategoriesResult.postValue(repo.getAll())
    }
}
