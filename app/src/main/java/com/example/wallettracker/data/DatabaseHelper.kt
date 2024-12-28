package com.example.wallettracker.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create both tables
        db.execSQL(CREATE_TABLE_EXPENSE_CATEGORY)
        db.execSQL(CREATE_TABLE_EXPENSE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop old tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_CATEGORY)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE)
        // Recreate tables
        onCreate(db)
    }

    companion object {
        // Database constants
        private const val DATABASE_NAME = "walletTracker.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_EXPENSE = "Expense"
        private const val TABLE_EXPENSE_CATEGORY = "ExpenseCategory"

        // SQL statement to create the Expense table
        private const val CREATE_TABLE_EXPENSE = "CREATE TABLE " + TABLE_EXPENSE + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "price DOUBLE, " +
                "expenseDate DATE, " +
                "category BIGINT, " +
                "FOREIGN KEY(category) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(_id) ON DELETE CASCADE);"

        // SQL statement to create the ExpenseCategory table
        private const val CREATE_TABLE_EXPENSE_CATEGORY =
            "CREATE TABLE " + TABLE_EXPENSE_CATEGORY + " (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL);"
    }
}
