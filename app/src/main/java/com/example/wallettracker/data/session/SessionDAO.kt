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

    // Insert a new Session into the database
    fun insert(session: Session): Long {
        val values = ContentValues()
        values.put("userId", session.userId)
        values.put("publicKey", session.publicKey)
        return database!!.insert("Session", null, values)
    }


    // Delete a Session from the database
    fun delete(SessionId: Long) {
        database!!.delete("Session", "_id = ?", arrayOf(SessionId.toString()))
    }
    fun deleteByUserId(userId: Int) {
        database!!.delete("Session", "userId = ?", arrayOf(userId.toString()))
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun getById(catId: Int): Session {
        var cat: Session? = null
        val cursor = database!!.rawQuery("SELECT * FROM Session WHERE userId = ${catId}", null)
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
    fun getByUserId(userId: Int): Session {
        var cat: Session? = null
        val cursor = database!!.rawQuery("SELECT * FROM Session WHERE userId = ${userId}", null)
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
    fun getAll(): ArrayList<Session> {
        var cat: Session? = null
        val cursor = database!!.rawQuery("SELECT * FROM Session", null)
        if (cursor != null) {
            cursor.moveToFirst()
            if(cursor.isFirst){
                cat = cursor(cursor)
            }
            cursor.close()
        }
        val a = ArrayList<Session>()
        a.add(cat!!)
        return a
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): Session {
        val sess = Session()
        sess.id = cursor.getInt(cursor.getColumnIndex("id"))
        sess.userId = cursor.getInt(cursor.getColumnIndex("userId"))
        sess.publicKey = cursor.getString(cursor.getColumnIndex("publicKey"))
        return sess
    }


}