package win.downops.wallettracker.ui.createExpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.di.ExpenseCategoryRepositoryProvider
import win.downops.wallettracker.di.ExpenseRepositoryProvider

@HiltViewModel
class CreateExpenseViewModel @Inject constructor(
    private val expenseRepo: ExpenseRepository,
    private val categoryRepo: ExpenseCategoryRepository
) : ViewModel() {

    private val _getExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val getExpenseResult: LiveData<AppResult<Expense?>> = _getExpenseResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpense(id: Long) = viewModelScope.launch {
        _getExpenseResult.postValue(expenseRepo.getById(id))
    }

    private val _createExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val createExpenseResult: LiveData<AppResult<Expense?>> = _createExpenseResult

    fun createExpense(expense: Expense) = viewModelScope.launch {
        _createExpenseResult.postValue(expenseRepo.create(expense))
    }

    private val _editExpenseResult = MutableLiveData<AppResult<Expense?>>()
    val editExpenseResult: LiveData<AppResult<Expense?>> = _editExpenseResult

    fun editExpense(expense: Expense) = viewModelScope.launch {
        _editExpenseResult.postValue(expenseRepo.edit(expense))
    }

    private val _deleteExpenseResult = MutableLiveData<AppResult<Unit>>()
    val deleteExpenseResult: LiveData<AppResult<Unit>> = _deleteExpenseResult

    fun deleteExpense(id: Long) = viewModelScope.launch {
        _deleteExpenseResult.postValue(expenseRepo.deleteById(id))
    }

    private val _getCategoriesResult = MutableLiveData<AppResult<List<ExpenseCategory>>>()
    val getCategoriesResult: LiveData<AppResult<List<ExpenseCategory>>> = _getCategoriesResult

    fun getCategories() = viewModelScope.launch {
        _getCategoriesResult.postValue(categoryRepo.getAll())
    }
}
