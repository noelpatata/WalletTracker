package com.example.wallettracker.data.session

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.DatabaseHelper
import java.io.Closeable
import java.sql.SQLException
import kotlin.Int
import kotlin.Long
import kotlin.Throws
import kotlin.arrayOf


class SessionDAO : Closeable{
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

    // Update an existing Session in the database
    fun edit(session: Session): Int {
        val values = ContentValues()
        values.put("userId", session.userId)
        values.put("token", session.token)
        values.put("privateKey", session.privateKey)
        values.put("serverPublicKey", session.serverPublicKey)
        values.put("remember", if (session.remember) 1 else 0)

        return database!!.update("Session", values, "id = ?", arrayOf(session.id.toString()))
    }


    // Insert a new Session into the database
    fun insert(session: Session): Long {
        val values = ContentValues()
        values.put("userId", session.userId)
        values.put("token", session.token)
        values.put("privateKey", session.privateKey)
        values.put("serverPublicKey", session.serverPublicKey)
        values.put("remember", if (session.remember) 1 else 0)
        values.put("online", if (session.online) 1 else 0)
        return database!!.insert("Session", null, values)
    }


    fun deleteAll() {
        database!!.delete("Session", null, null)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFirstSession(): Session? {
        var cat: Session? = null
        val cursor = database!!.rawQuery("SELECT * FROM Session LIMIT 1", null)
        if (cursor != null) {
            cursor.moveToFirst()
            if(cursor.isFirst){
                cat = cursor(cursor)
            }
            cursor.close()
        }
        return cat
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): Session {
        return Session().apply {
            id = cursor.getInt(cursor.getColumnIndex("id"))
            userId = cursor.getInt(cursor.getColumnIndex("userId"))
            token = cursor.getString(cursor.getColumnIndex("token"))
            privateKey = cursor.getString(cursor.getColumnIndex("privateKey"))
            serverPublicKey = cursor.getString(cursor.getColumnIndex("serverPublicKey"))
            remember = cursor.getInt(cursor.getColumnIndex("remember")) == 1
            online = cursor.getInt(cursor.getColumnIndex("online")) == 1
        }
    }


}