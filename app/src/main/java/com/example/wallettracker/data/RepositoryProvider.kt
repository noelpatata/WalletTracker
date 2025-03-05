import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.OfflineExpenseDAO.OfflineExpenseDAO
import com.example.wallettracker.data.expense.OnlineExpenseDAO
import com.example.wallettracker.data.expenseCategory.OfflineExpenseCategoryDAO
import com.example.wallettracker.data.expenseCategory.OnlineExpenseCategoryDAO
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRepository
import com.example.wallettracker.data.expense.ExpenseRepository
import com.example.wallettracker.data.session.SessionDAO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
fun isOnline(context: Context): Boolean {
    val session = SessionDAO(context).getFirstSession()
    return session?.online ?: true
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun provideExpenseRepository(context: Context): ExpenseRepository =
    withContext(Dispatchers.IO) {
        if (isOnline(context)) {
            OnlineExpenseDAO(context)
        } else {
            OfflineExpenseDAO(context)
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
suspend fun provideExpenseCategoryRepository(context: Context): ExpenseCategoryRepository =
    withContext(Dispatchers.IO) {
        if (isOnline(context)) {
            OnlineExpenseCategoryDAO(context)
        } else {
            OfflineExpenseCategoryDAO(context)
        }
    }
