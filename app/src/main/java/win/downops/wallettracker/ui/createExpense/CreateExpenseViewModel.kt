package win.downops.wallettracker.ui.createExpense

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CreateExpenseViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Create Expense fragment"
    }
    val text: LiveData<String> = _text
}