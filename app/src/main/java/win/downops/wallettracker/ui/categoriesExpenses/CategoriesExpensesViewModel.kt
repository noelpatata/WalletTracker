package win.downops.wallettracker.ui.categoriesExpenses

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.models.ExpenseCategory


@HiltViewModel
class CategoriesExpensesViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val expenseCategoryRepository: ExpenseCategoryRepository
) : ViewModel() {

    private val _deleteResult = MutableLiveData<AppResult<Unit>>()
    val deleteResult: LiveData<AppResult<Unit>> = _deleteResult
    fun deleteExpense(expenseId: Long) = viewModelScope.launch {
        _deleteResult.postValue(expenseRepository.deleteById(expenseId))
    }

    private val _getExpenseCategoryByIdResult = MutableLiveData<AppResult<ExpenseCategory?>>()
    val getByExpenseCategoryByIdResult: LiveData<AppResult<ExpenseCategory?>> = _getExpenseCategoryByIdResult
    fun getExpenseCategoryById(categoryId: Long) = viewModelScope.launch {
        _getExpenseCategoryByIdResult.postValue(expenseCategoryRepository.getById(categoryId))
    }

    private val _getExpensesByCategoryIdResult = MutableLiveData<AppResult<List<Expense>>>()
    val getExpensesByCategoryIdResult: LiveData<AppResult<List<Expense>>> = _getExpensesByCategoryIdResult
    fun getExpensesByCategoryId(categoryId: Long) = viewModelScope.launch {
        _getExpensesByCategoryIdResult.postValue(expenseRepository.getByCatId(categoryId))
    }

    private val _editExpenseCategoryResult = MutableLiveData<AppResult<ExpenseCategory?>>()
    val editExpenseCategoryResult: LiveData<AppResult<ExpenseCategory?>> = _editExpenseCategoryResult
    fun editExpenseCategory(expenseCategory: ExpenseCategory) = viewModelScope.launch {
        _editExpenseCategoryResult.postValue(expenseCategoryRepository.edit(expenseCategory))
    }

    private val _deleteExpenseCategoryResult = MutableLiveData<AppResult<Unit>>()
    val deleteExpenseCategoryResult: LiveData<AppResult<Unit>> = _deleteExpenseCategoryResult
    fun deleteExpenseCategory(categoryId: Long) = viewModelScope.launch {
        _deleteExpenseCategoryResult.postValue(expenseCategoryRepository.deleteById(categoryId))
    }


}
