package com.example.wallettracker.data.expenseCategory

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
import java.text.SimpleDateFormat
import java.util.Locale
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
    fun insert(Expense: Expense): Long {
        val values = ContentValues()
        values.put("price", Expense.getPrice())
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        values.put("expenseDate", format.format(Expense.getDate()))
        values.put("category", Expense.getCategoryId())
        return database!!.insert("Expense", null, values)
    }

    // Update an existing Expense in the database
    fun update(Expense: Expense): Int {
        val values = ContentValues()
        values.put("price", Expense.getPrice())
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        values.put("expenseDate", format.format(Expense.getDate()))
        return database!!.update("Expense", values, "_id = ?", arrayOf(String.valueOf(Expense.getId())))
    }

    // Delete a Expense from the database
    fun delete(ExpenseId: Long) {
        database!!.delete("Expense", "_id = ?", arrayOf(ExpenseId.toString()))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getById(catId: Long): Expense {
        var cat: Expense? = null
        val cursor = database!!.rawQuery("SELECT * FROM Expense WHERE _id = ${catId}", null)
        if (cursor != null) {
            cursor.moveToFirst()
            if(cursor.isFirst){
                cat = cursor(cursor)
            }
            cursor.close()
        }
        return cat!!
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getByCategory(catId: Long): List<Expense>? {
        val ExpenseList: MutableList<Expense> = ArrayList<Expense>()
        val cursor = database!!.rawQuery("SELECT * from Expense where category = ${catId} ORDER BY _id DESC", null)

        if (cursor != null) {
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val Expense: Expense = cursor(cursor)
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
        val cursor = database!!.rawQuery("SELECT SUM(price) as total from Expense where category = ${categoryId}", null)
        var total = 0.0
        if(cursor.count > 0){
            cursor.moveToFirst()
            total = cursor.getDouble(cursor.getColumnIndex("total"))
        }

        return total
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): Expense {
        val Expense = Expense(cursor.getLong(cursor.getColumnIndex("_id")))
        Expense.setPrice(cursor.getDouble(cursor.getColumnIndex("price")))

        val dateString = cursor.getString(cursor.getColumnIndex("expenseDate"))
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate = format.parse(dateString)
        val sqlDate = java.sql.Date(parsedDate.time)
        Expense.setDate(sqlDate)

        val categoryId = cursor.getLong(cursor.getColumnIndex("category"))
        Expense.setCategoryId(categoryId)

        return Expense
    }


}