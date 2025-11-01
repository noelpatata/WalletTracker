package win.downops.wallettracker.ui.categories

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
class CategoriesViewModel @Inject constructor(
    private val repo: ExpenseCategoryRepository
) : ViewModel() {

    private val _getCategoriesResult = MutableLiveData<AppResult<List<ExpenseCategory>>>()
    val getCategoriesResult: LiveData<AppResult<List<ExpenseCategory>>> = _getCategoriesResult
    fun getCategories() = viewModelScope.launch {
        _getCategoriesResult.postValue(repo.getAll())
    }

    private val _editCategoryResult = MutableLiveData<AppResult<ExpenseCategory?>>()
    val editCategoryResult: LiveData<AppResult<ExpenseCategory?>> = _editCategoryResult
    fun editCategory(category: ExpenseCategory) = viewModelScope.launch {
        _editCategoryResult.postValue(repo.edit(category))
    }

    private val _deleteCategoryResult = MutableLiveData<AppResult<Unit>>()
    val deleteCategoryResult: LiveData<AppResult<Unit>> = _deleteCategoryResult
    fun deleteCategory(id: Long) = viewModelScope.launch {
        _deleteCategoryResult.postValue(repo.deleteById(id))
    }
}
