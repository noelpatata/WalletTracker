package com.example.wallettracker.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_SESSION)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION)
        // Recreate tables
        onCreate(db)
    }

    companion object {
        // Database constants
        private const val DATABASE_NAME = "walletTracker.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_SESSION = "Session"

        // SQL statement to create the Expense table
        private const val CREATE_TABLE_SESSION = "CREATE TABLE " + TABLE_SESSION + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "price DOUBLE, " +
                "expenseDate DATE, " +
                "category INTEGER, ;"
    }
}
