package com.example.wallettracker.data
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SESSION)
        db.execSQL(CREATE_TABLE_EXPENSE_CATEGORY)
        db.execSQL(CREATE_TABLE_EXPENSE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE_CATEGORY)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE)
        // Recreate tables
        onCreate(db)
    }
    companion object {
        // Database constants
        private const val DATABASE_NAME = "walletTracker.db"
        private const val DATABASE_VERSION = 3
        // Table names
        private const val TABLE_SESSION = "Session"
        // SQL statement to create the Expense table
        private const val CREATE_TABLE_SESSION = "CREATE TABLE " + TABLE_SESSION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "online Integer, " +
                "userId Integer, " +
                "token TEXT, " +
                "privateKey TEXT, " +
                "remember INTEGER, " +
                "serverPublicKey TEXT);"
        private const val TABLE_EXPENSE = "Expense"
        private const val TABLE_EXPENSE_CATEGORY = "ExpenseCategory"

        private const val CREATE_TABLE_EXPENSE_CATEGORY =
            "CREATE TABLE " + TABLE_EXPENSE_CATEGORY + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "sortOrder Integer NULL, " +
                    "name TEXT NOT NULL);"
        private const val CREATE_TABLE_EXPENSE = "CREATE TABLE " + TABLE_EXPENSE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "price DOUBLE, " +
                "description TEXT, " +
                "expenseDate DATE, " +
                "category INTEGER, " +
                "FOREIGN KEY(category) REFERENCES " + TABLE_EXPENSE_CATEGORY + "(id) ON DELETE CASCADE);"


    }
}