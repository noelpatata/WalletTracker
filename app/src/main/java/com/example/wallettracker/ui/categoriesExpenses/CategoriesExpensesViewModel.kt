package com.example.wallettracker.ui.categoriesExpenses

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallettracker.data.expense.ExpenseRepository
import kotlinx.coroutines.launch
import provideExpenseRepository

class CategoriesExpensesViewModel : ViewModel() {

    private val _deleteResult = MutableLiveData<Result<String>>()
    val deleteResult: LiveData<Result<String>> = _deleteResult

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteExpense(context: Context, expenseId: Long) {
        viewModelScope.launch {
            try {
                val expenseDAO: ExpenseRepository = provideExpenseRepository(context)

                expenseDAO.deleteById(
                    onSuccess = { response ->
                        if (!response.success) {
                            _deleteResult.postValue(Result.failure(Exception(response.message)))
                        } else {
                            _deleteResult.postValue(Result.success("Deleted successfully"))
                        }
                    },
                    onFailure = { error ->
                        _deleteResult.postValue(Result.failure(Exception(error)))
                    },
                    expenseId = expenseId
                )
            } catch (e: Exception) {
                _deleteResult.postValue(Result.failure(e))
            }
        }
    }
}
