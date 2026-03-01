package win.downops.wallettracker.data.sqlite.expense

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import win.downops.wallettracker.data.sqlite.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import win.downops.wallettracker.data.models.AppResult
import win.downops.wallettracker.data.models.Expense
import win.downops.wallettracker.data.ExpenseRepository
import java.io.Closeable
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import kotlin.Long
import kotlin.Throws

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseSqlService @Inject constructor(
    @ApplicationContext context: Context?
) : Closeable, ExpenseRepository {

    private var database: SQLiteDatabase? = null
    private var dbHelper: DatabaseHelper? = DatabaseHelper(context)

    init {
        database = dbHelper?.writableDatabase
    }

    @Throws(SQLException::class)
    fun open() {
        database = dbHelper?.writableDatabase
    }

    override fun close() {
        dbHelper?.close()
    }

    @SuppressLint("SimpleDateFormat")
    override suspend fun create(expense: Expense): AppResult<Expense?> = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put("price", expense.getPrice())
                put("expenseDate", SimpleDateFormat("yyyy-MM-dd").format(expense.getDate()))
                put("category", expense.getCategoryId())
                put("description", expense.getDescription())
            }

            val rowId = database?.insert("Expense", null, values)
                ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            if (rowId == -1L)
                return@withContext AppResult.Error("Failed to create expense", isControlled = true)

            AppResult.Success("Expense created successfully", expense)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error creating expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    @SuppressLint("SimpleDateFormat")
    override suspend fun edit(expense: Expense): AppResult<Expense?> = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put("price", expense.getPrice())
                put("category", expense.getCategoryId())
                put("description", expense.getDescription())
                put("expenseDate", SimpleDateFormat("yyyy-MM-dd").format(expense.getDate()))
            }

            val rowsAffected = database?.update("Expense", values, "id = ?", arrayOf(expense.getId().toString()))
                ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            if (rowsAffected == 0)
                return@withContext AppResult.Error("Failed to update expense", isControlled = true)

            AppResult.Success("Expense modified successfully", expense)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error updating expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteById(expenseId: Long): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val rowsDeleted = database?.delete("Expense", "id = ?", arrayOf(expenseId.toString()))
                ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            if (rowsDeleted == 0)
                return@withContext AppResult.Error("Failed to delete expense", isControlled = true)

            AppResult.Success("Expense deleted successfully", Unit)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error deleting expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun deleteAll(): AppResult<Unit> = withContext(Dispatchers.IO) {
        try {
            val rowsDeleted = database?.delete("Expense", null, null)
                ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            if (rowsDeleted == 0)
                return@withContext AppResult.Error("No expenses found to delete", isControlled = true)

            AppResult.Success("Expenses deleted successfully", Unit)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error deleting all expenses",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getByCatId(catId: Long): AppResult<List<Expense>> = withContext(Dispatchers.IO) {
        try {
            val expenseList = mutableListOf<Expense>()
            val cursor = database?.query(
                "Expense",
                null,
                "category = ?",
                arrayOf(catId.toString()),
                null,
                null,
                "expenseDate DESC, id DESC"
            ) ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            cursor.use {
                while (it.moveToNext()) {
                    expenseList.add(mapCursor(it))
                }
            }

            if (expenseList.isEmpty())
                return@withContext AppResult.Error("No expenses found for category ID: $catId", isControlled = true)

            AppResult.Success("Expenses fetched successfully", expenseList)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error reading expenses",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    override suspend fun getById(expenseId: Long): AppResult<Expense?> = withContext(Dispatchers.IO) {
        try {
            val cursor = database?.query(
                "Expense",
                null,
                "id = ?",
                arrayOf(expenseId.toString()),
                null,
                null,
                null
            ) ?: return@withContext AppResult.Error("Database not available", isControlled = true)

            var expense: Expense? = null
            cursor.use {
                if (it.moveToFirst()) {
                    expense = mapCursor(it)
                }
            }

            if (expense == null)
                return@withContext AppResult.Error("Expense not found with ID: $expenseId", isControlled = true)

            AppResult.Success("Expense fetched successfully", expense!!)
        } catch (e: Exception) {
            AppResult.Error(
                e.message ?: "Unexpected error fetching expense",
                isControlled = false,
                e.stackTrace.joinToString("\n"))
        }
    }

    @SuppressLint("Range")
    private fun mapCursor(cursor: Cursor): Expense {
        try{
            return Expense(cursor.getLong(cursor.getColumnIndex("id"))).apply {
                setDescription(cursor.getString(cursor.getColumnIndex("description")))
                setPrice(cursor.getDouble(cursor.getColumnIndex("price")))
                setDate(Date.valueOf(cursor.getString(cursor.getColumnIndex("expenseDate"))))
                setCategoryId(cursor.getLong(cursor.getColumnIndex("category")))
            }
        }catch (e: Exception) {
            throw e
        }
    }
}