import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import win.downops.wallettracker.data.api.expense.ExpenseHttpService
import win.downops.wallettracker.data.sqlite.expense.ExpenseSqlService
import win.downops.wallettracker.data.sqlite.expenseCategory.ExpenseCategorySqlService
import win.downops.wallettracker.data.api.expenseCategory.ExpenseCategoryHttpService
import win.downops.wallettracker.data.ExpenseCategoryRepository
import win.downops.wallettracker.data.ExpenseRepository
import win.downops.wallettracker.data.sqlite.session.SessionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@RequiresApi(Build.VERSION_CODES.O)
fun isOnline(context: Context): Boolean {
    val session = SessionService(context).getFirstSession()
    return session?.online ?: true
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun provideExpenseRepository(context: Context): ExpenseRepository =
    withContext(Dispatchers.IO) {
        if (isOnline(context)) {
            ExpenseHttpService(context)
        } else {
            ExpenseSqlService(context)
        }
    }

@RequiresApi(Build.VERSION_CODES.O)
suspend fun provideExpenseCategoryRepository(context: Context): ExpenseCategoryRepository =
    withContext(Dispatchers.IO) {
        if (isOnline(context)) {
            ExpenseCategoryHttpService(context)
        } else {
            ExpenseCategorySqlService(context)
        }
    }
