package win.downops.wallettracker.ui.createCategories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.ExpenseCategory
import win.downops.wallettracker.di.ExpenseCategoryRepositoryProvider

@HiltViewModel
class CreateCategoriesViewModel @Inject constructor(
    private val expenseCategoryProvider: ExpenseCategoryRepositoryProvider
) : ViewModel() {

    private val _createCategoryResult = MutableLiveData<AppResult<ExpenseCategory?>>()
    val createCategoryResult: LiveData<AppResult<ExpenseCategory?>> = _createCategoryResult

    fun createCategory(category: ExpenseCategory) = viewModelScope.launch {
        val repo = expenseCategoryProvider.get()
        _createCategoryResult.postValue(repo.create(category))
    }
}
