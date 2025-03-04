package com.example.wallettracker.data.OfflineExpenseDAO

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expense.Expense
import com.example.wallettracker.data.interfaces.ExpenseRepository
import java.io.Closeable
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import kotlin.Long
import kotlin.Throws
import kotlin.arrayOf

class OfflineExpenseDAO : Closeable, ExpenseRepository {
    private var database: SQLiteDatabase? = null
    private var dbHelper: DatabaseHelper? = null

    constructor(context: Context?) {
        dbHelper = DatabaseHelper(context)
        database = dbHelper?.getWritableDatabase()
    }

    // Open the database for read/write operations
    @Throws(SQLException::class)
    fun open() {
        database = dbHelper?.getWritableDatabase()
    }

    // Close the database
    override fun close() {
        dbHelper?.close()
    }

    // Insert a new Expense into the database asynchronously
    override fun createExpense(
        expense: Expense,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val values = ContentValues().apply {
                put("price", expense.getPrice())
                val format = SimpleDateFormat("yyyy-MM-dd")
                put("expenseDate", format.format(expense.getDate()))
                put("category", expense.getCategoryId())
            }

            val rowId = database!!.insert("Expense", null, values)
            if (rowId != -1L) {
                onSuccess(expense)
            } else {
                onFailure("Failed to create expense")
            }
        }.start()
    }

    // Update an existing Expense in the database asynchronously
    override fun edit(
        expense: Expense,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val values = ContentValues().apply {
                put("price", expense.getPrice())
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                put("expenseDate", format.format(expense.getDate()))
            }

            val rowsAffected = database!!.update("Expense", values, "id = ?", arrayOf(expense.getId().toString()))
            if (rowsAffected > 0) {
                onSuccess(expense)
            } else {
                onFailure("Failed to update expense")
            }
        }.start()
    }

    // Delete an Expense by id asynchronously
    override fun deleteById(
        expenseId: Long,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val rowsDeleted = database!!.delete("Expense", "id = ?", arrayOf(expenseId.toString()))
            if (rowsDeleted > 0) {
                onSuccess(SuccessResponse(true, "Expense deleted successfully"))
            } else {
                onFailure("Failed to delete expense")
            }
        }.start()
    }

    // Delete all Expenses asynchronously
    override fun deleteAll(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val rowsDeleted = database!!.delete("Expense", null, null)
            if (rowsDeleted > 0) {
                onSuccess(SuccessResponse(true, "All expenses deleted successfully"))
            } else {
                onFailure("Failed to delete all expenses")
            }
        }.start()
    }

    // Retrieve all Expenses asynchronously
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getByCatId(
        catId: Long,
        onSuccess: (List<Expense>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val expenseList: MutableList<Expense> = ArrayList()
            val cursor = database!!.query("Expense", null, "category = ?", arrayOf(catId.toString()), null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                while (!cursor.isAfterLast) {
                    val expense = cursorToExpense(cursor)
                    expenseList.add(expense)
                    cursor.moveToNext()
                }
                cursor.close()

                if (expenseList.isNotEmpty()) {
                    onSuccess(expenseList)
                } else {
                    onFailure("No expenses found for the given category")
                }
            } else {
                onFailure("Failed to fetch expenses")
            }
        }.start()
    }

    // Retrieve an Expense by id asynchronously
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getById(
        expenseId: Long,
        onSuccess: (Expense) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Thread {
            val cursor = database!!.query("Expense", null, "id = ?", arrayOf(expenseId.toString()), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val expense = cursorToExpense(cursor)
                cursor.close()
                onSuccess(expense)
            } else {
                onFailure("Expense not found")
            }
        }.start()
    }

    // Helper function to convert cursor data into Expense
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursorToExpense(cursor: Cursor): Expense {
        val expense = Expense()
        expense.setPrice(cursor.getDouble(cursor.getColumnIndex("price")))

        val dateString = cursor.getString(cursor.getColumnIndex("expenseDate"))
        expense.setDate(Date.valueOf(dateString))

        return expense
    }

    // Retrieve all Expenses from the database synchronously (for utility purposes)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllExpenses(): List<Expense>? {
        val expenseList: MutableList<Expense> = ArrayList()
        val cursor = database!!.query("Expense", null, null, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val expense = cursorToExpense(cursor)
                expenseList.add(expense)
                cursor.moveToNext()
            }
            cursor.close()
        }
        return expenseList
    }

    // Retrieve the total price for a category
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    fun getByCategory(categoryId: Long): Double {
        val cursor = database!!.rawQuery("SELECT SUM(price) as total FROM Expense WHERE category = ?", arrayOf(categoryId.toString()))
        var total: Double = 0.0
        if (cursor.count > 0) {
            cursor.moveToFirst()
            total = cursor.getDouble(cursor.getColumnIndex("total"))
        }
        cursor.close()
        return total
    }
}
