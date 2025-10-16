package win.downops.wallettracker.ui.categoriesExpenses

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import provideExpenseRepository
import win.downops.wallettracker.data.models.AppResult

class CategoriesExpensesViewModel : ViewModel() {

    private val _deleteResult = MutableLiveData<AppResult<Unit>>()
    val deleteResult: LiveData<AppResult<Unit>> = _deleteResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExpense(context: Context, expenseId: Long) {
        viewModelScope.launch {
            val expenseDAO = provideExpenseRepository(context)
            when (val result = expenseDAO.deleteById(expenseId)) {
                is AppResult.Success -> {
                    _deleteResult.postValue(result)
                }
                is AppResult.Error -> {
                    _deleteResult.postValue(result)
                }
            }
        }
    }


}
