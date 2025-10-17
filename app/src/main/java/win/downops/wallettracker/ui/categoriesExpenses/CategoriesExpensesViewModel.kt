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
import win.downops.wallettracker.di.ExpenseCategoryRepositoryProvider
import win.downops.wallettracker.di.ExpenseRepositoryProvider


@HiltViewModel
class CategoriesExpensesViewModel @Inject constructor(
    private val repositoryProvider: ExpenseRepositoryProvider,
    private val expenseCategoryRepository: ExpenseCategoryRepositoryProvider
) : ViewModel() {

    private val _deleteResult = MutableLiveData<AppResult<Unit>>()
    val deleteResult: LiveData<AppResult<Unit>> = _deleteResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExpense(expenseId: Long) = viewModelScope.launch {
        val expenseRepository = repositoryProvider.get()
        _deleteResult.postValue(expenseRepository.deleteById(expenseId))
    }
}
