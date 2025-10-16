package win.downops.wallettracker.ui.categoriesExpenses

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import win.downops.wallettracker.data.expense.ExpenseRepository
import win.downops.wallettracker.data.login.AppResult
import kotlinx.coroutines.launch
import provideExpenseRepository

class CategoriesExpensesViewModel : ViewModel() {

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExpense(context: Context, expenseId: Long) {
        viewModelScope.launch {
            val expenseDAO: ExpenseRepository = provideExpenseRepository(context)
            when (val result = expenseDAO.deleteById(expenseId)) {
                is AppResult.Success -> _deleteResult.postValue(Result.success(result.message))
                is AppResult.Error -> _deleteResult.postValue(Result.failure(Exception(result.message)))
            }
        }
    }
}
