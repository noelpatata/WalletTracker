package com.example.wallettracker.data.OfflineExpenseDAO

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import com.example.wallettracker.data.communication.SuccessResponse
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.expense.ExpenseRepository
import java.io.Closeable
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import kotlin.Long
import kotlin.Throws

class OfflineExpenseDAO(context: Context?) : Closeable, ExpenseRepository {
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

    private fun <T> executeAsyncTask(
        task: () -> T,
        onSuccess: (T) -> Unit,
        onFailure: () -> Unit
    ) {
        Thread {
            try {
                val result = task()
                Handler(Looper.getMainLooper()).post { onSuccess(result) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onFailure() }
            }
        }.start()
    }
    private fun <T> executeAsyncListTask(
        task: () -> List<T>,
        onSuccess: (List<T>) -> Unit,
        onFailure: () -> Unit
    ) {
        Thread {
            try {
                val result = task()
                Handler(Looper.getMainLooper()).post { onSuccess(result) }
            } catch (e: Exception) {
                Handler(Looper.getMainLooper()).post { onFailure() }
            }
        }.start()
    }

    override fun create(expense: Expense, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        executeAsyncTask(
            task = {
                val values = ContentValues().apply {
                    put("price", expense.getPrice())
                    put("expenseDate", SimpleDateFormat("yyyy-MM-dd").format(expense.getDate()))
                    put("category", expense.getCategoryId())
                    put("description", expense.getDescription())
                }
                val rowId = database!!.insert("Expense", null, values)
                if (rowId == -1L) throw Exception("Failed to create expense")
            },
            onSuccess = { onSuccess(expense) },
            onFailure = { onFailure("Failed to create expense") }
        )
    }

    override fun edit(expense: Expense, onSuccess: (Expense) -> Unit, onFailure: (String) -> Unit) {
        executeAsyncTask(
            task = {
                val values = ContentValues().apply {
                    put("price", expense.getPrice())
                    put("category", expense.getCategoryId())
                    put("description", expense.getDescription())
                    put("expenseDate", SimpleDateFormat("yyyy-MM-dd").format(expense.getDate()))
                }
                val rowsAffected = database!!.update("Expense", values, "id = ?", arrayOf(expense.getId().toString()))
                if (rowsAffected == 0) throw Exception("Failed to update expense")
            },
            onSuccess = { onSuccess(expense) },
            onFailure = { onFailure("Failed to update expense") }
        )
    }

    override fun deleteById(expenseId: Long, onSuccess: (SuccessResponse) -> Unit, onFailure: (String) -> Unit) {
        executeAsyncTask(
            task = {
                val rowsDeleted = database!!.delete("Expense", "id = ?", arrayOf(expenseId.toString()))
                if (rowsDeleted == 0) throw Exception("Failed to delete expense")
            },
            onSuccess = { onSuccess(SuccessResponse(true, "Expense deleted successfully")) },
            onFailure = { onFailure("Failed to delete expense") }
        )
    }

    override fun deleteAll(onSuccess: (SuccessResponse) -> Unit, onFailure: (String) -> Unit) {
        executeAsyncTask(
            task = {
                val rowsDeleted = database!!.delete("Expense", null, null)
                if (rowsDeleted == 0) throw Exception("Failed to delete all expenses")
            },
            onSuccess = { onSuccess(SuccessResponse(true, "All expenses deleted successfully")) },
            onFailure = { onFailure("Failed to delete all expenses") }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getByCatId(catId: Long, onSuccess: (List<Expense>) -> Unit, onFailure: (String) -> Unit) {
        executeAsyncListTask(
            task = {
                val expenseList = mutableListOf<Expense>()
                database!!.query("Expense", null, "category = ?", arrayOf(catId.toString()), null, null, "expenseDate DESC, id DESC")?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val expense = cursor(cursor)
                        expenseList.add(expense)

                    }
                }
                expenseList

            },
            onSuccess = { result -> onSuccess(result) },
            onFailure = { onFailure("No expenses found for the given category") }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getById(
        expenseId: Long,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    ) {
        executeAsyncTask(
            task = {
                var expense: Expense? = null
                val cursor = database!!.query("Expense", null, "id = ?", arrayOf(expenseId.toString()), null, null, null)
                cursor.use {
                    if (it.moveToFirst()) {
                        expense = cursor(it)
                    }
                }
                if (expense == null) throw Exception("Expense not found")
                expense // Store the result
            },
            onSuccess = { result -> onSuccess(result as Expense) },
            onFailure = { onFailure("Expense not found") }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): Expense {
        return Expense(cursor.getLong(cursor.getColumnIndex("id"))).apply {
            setDescription(cursor.getString(cursor.getColumnIndex("description")))
            setPrice(cursor.getDouble(cursor.getColumnIndex("price")))
            setDate(Date.valueOf(cursor.getString(cursor.getColumnIndex("expenseDate"))))
            setCategoryId(cursor.getLong(cursor.getColumnIndex("category")))
        }
    }
}
