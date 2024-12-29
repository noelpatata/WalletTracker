package com.example.wallettracker.data.ExpenseCategory

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import java.io.Closeable
import java.lang.String
import java.sql.Date
import java.sql.SQLException
import java.text.SimpleDateFormat
import kotlin.Int
import kotlin.Long
import kotlin.Throws
import kotlin.arrayOf


class ExpenseDAO : Closeable{
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

    // Insert a new Expense into the database
    fun insertExpense(Expense: Expense): Long {
        val values = ContentValues()
        values.put("price", Expense.getPrice())
        val format = SimpleDateFormat("yyyy-MM-dd")
        values.put("expenseDate", format.format(Expense.getDate()))
        values.put("category", Expense.getCategoryId())
        return database!!.insert("Expense", null, values)
    }

    // Update an existing Expense in the database
    fun updateExpense(Expense: Expense): Int {
        val values = ContentValues()
        values.put("price", Expense.getPrice())
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        values.put("expenseDate", format.format(Expense.getDate()))
        return database!!.update("Expense", values, "_id = ?", arrayOf(String.valueOf(Expense.getId())))
    }

    // Delete a Expense from the database
    fun deleteExpense(ExpenseId: Long) {
        database!!.delete("Expense", "_id = ?", arrayOf(ExpenseId.toString()))
    }

    // Retrieve a list of all Expenses from the database
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllExpenses(): List<Expense>? {
        val ExpenseList: MutableList<Expense> = ArrayList<Expense>()
        val cursor = database!!.query("Expense", null, null, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val Expense: Expense = cursorToExpense(cursor)
                ExpenseList.add(Expense)
                cursor.moveToNext()
            }
            cursor.close()
        }
        return ExpenseList
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getByCategory(catId: Long): List<Expense>? {
        val ExpenseList: MutableList<Expense> = ArrayList<Expense>()
        val cursor = database!!.rawQuery("SELECT * from Expense where category = ${catId}", null)

        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val Expense: Expense = cursorToExpense(cursor)
                ExpenseList.add(Expense)
                cursor.moveToNext()
            }
            cursor.close()
        }
        return ExpenseList
    }
    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.O)
    fun getByTotalCategory(categoryId: Long): Double {
        val catid = categoryId.toInt()
        val ExpenseList: MutableList<Expense> = ArrayList<Expense>()
        val cursor = database!!.rawQuery("SELECT SUM(price) as total from Expense where category = ${categoryId}", null)
        var total: Double = 0.0
        if(cursor.count > 0){
            cursor.moveToFirst()
            total = cursor.getDouble(cursor.getColumnIndex("total"))
        }

        return total
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursorToExpense(cursor: Cursor): Expense {
        val Expense = Expense(cursor.getLong(cursor.getColumnIndex("_id")))
        Expense.setPrice(cursor.getDouble(cursor.getColumnIndex("price")))

        val datestring = cursor.getString(cursor.getColumnIndex("expenseDate"))
        Expense.setDate(Date.valueOf(datestring))

        return Expense
    }
}