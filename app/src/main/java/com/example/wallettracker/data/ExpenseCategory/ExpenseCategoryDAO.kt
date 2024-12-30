package com.example.wallettracker.data.Expense

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
import java.sql.SQLException
import kotlin.Int
import kotlin.Long
import kotlin.Throws
import kotlin.arrayOf


class ExpenseCategoryDAO : Closeable {
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
    fun insert(Expense: ExpenseCategory): Long {
        val values = ContentValues()
        values.put("name", Expense.getName())
        return database!!.insert("ExpenseCategory", null, values)
    }

    // Update an existing Expense in the database
    fun update(Expense: ExpenseCategory): Int {
        val values = ContentValues()
        values.put("name", Expense.getName())
        return database!!.update("ExpenseCategory", values, "_id = ?", arrayOf(String.valueOf(Expense.getId())))
    }

    // Delete a Expense from the database
    fun delete(ExpenseId: Long) {
        database!!.delete("ExpenseCategory", "_id = ?", arrayOf(ExpenseId.toString()))
    }

    // Retrieve a list of all Expenses from the database
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAll(): List<ExpenseCategory>? {
        val ExpenseList: MutableList<ExpenseCategory> = ArrayList<ExpenseCategory>()
        val cursor = database!!.query("ExpenseCategory", null, null, null, null, null, null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val Expense: ExpenseCategory = cursor(cursor)
                ExpenseList.add(Expense)
                cursor.moveToNext()
            }
            cursor.close()
        }
        return ExpenseList
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): ExpenseCategory {
        val Expense = ExpenseCategory(cursor.getLong(cursor.getColumnIndex("_id")))
        Expense.setName(cursor.getString(cursor.getColumnIndex("name")))

        return Expense
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getById(catId: Long): ExpenseCategory {
        var cat:ExpenseCategory? = null
        val cursor = database!!.rawQuery("SELECT * FROM ExpenseCategory WHERE _id = ${catId} ORDER BY name", null)
        if (cursor != null) {
            cursor.moveToFirst()
            if(cursor.isFirst){
                cat = cursor(cursor)
            }
            cursor.close()
        }
        return cat!!
    }
}