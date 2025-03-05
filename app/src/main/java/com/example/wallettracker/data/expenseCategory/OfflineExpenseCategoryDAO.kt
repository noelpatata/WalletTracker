package com.example.wallettracker.data.expenseCategory

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
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.interfaces.ExpenseCategoryRepository
import java.io.Closeable
import java.sql.SQLException

class OfflineExpenseCategoryDAO : Closeable, ExpenseCategoryRepository {
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
    override fun createExpenseCategories(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        executeAsyncTask(
            task = {
                val values = ContentValues().apply { put("name", category.getName()) }
                val rowId = database!!.insert("ExpenseCategory", null, values)
                if (rowId == -1L) {
                    throw Exception("Failed to create expense category")
                }
            },
            onSuccess = { onSuccess(category) },
            onFailure = { onFailure(SuccessResponse(false, "Failed to create expense category")) }
        )
    }

    // Update an existing ExpenseCategory in the database asynchronously
    override fun editName(
        category: ExpenseCategory,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        executeAsyncTask(
            task = {
                val values = ContentValues().apply { put("name", category.getName()) }
                val rowsAffected = database!!.update("ExpenseCategory", values, "id = ?", arrayOf(category.getId().toString()))
                if (rowsAffected <= 0) throw Exception("Failed to update category")
            },
            onSuccess = { onSuccess(SuccessResponse(true, "Category updated successfully")) },
            onFailure = { onFailure(SuccessResponse(false, "Failed to update category")) }
        )
    }

    // Delete an ExpenseCategory by id asynchronously
    override fun deleteById(
        catId: Long,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        executeAsyncTask(
            task = {
                val rowsDeleted = database!!.delete("ExpenseCategory", "id = ?", arrayOf(catId.toString()))
                if (rowsDeleted <= 0) throw Exception("Failed to delete category")
            },
            onSuccess = { onSuccess(SuccessResponse(true, "Category deleted successfully")) },
            onFailure = { onFailure(SuccessResponse(false, "Failed to delete category")) }
        )
    }

    // Retrieve all ExpenseCategories from the database asynchronously
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getExpenseCategories(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        executeAsyncTask(
            task = {
                val expenseCategoriesWithTotal = mutableListOf<ExpenseCategory>()
                val query = """
                SELECT ec.id, ec.name, SUM(e.price) AS total
                FROM ExpenseCategory ec
                LEFT JOIN Expense e ON ec.id = e.category
                GROUP BY ec.id
            """
                val cursor = database!!.rawQuery(query, null)
                cursor.use {
                    while (it.moveToNext()) {
                        val expenseCategory = cursor(it)
                        expenseCategoriesWithTotal.add(expenseCategory)
                    }
                }
                expenseCategoriesWithTotal
            },
            onSuccess = { categories -> onSuccess(categories) }, // Now runs on the main thread
            onFailure = { onFailure(SuccessResponse(false, "Failed to fetch categories")) }
        )
    }

    // Retrieve a single ExpenseCategory by id asynchronously
    @RequiresApi(Build.VERSION_CODES.O)
    override fun getExpenseCategoryById(
        catId: Long,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        executeAsyncTask(
            task = {
                val cursor = database!!.query("ExpenseCategory", null, "id = ?", arrayOf(catId.toString()), null, null, null)
                cursor.use {
                    if (it.moveToFirst()) {
                        return@executeAsyncTask cursor(it)
                    }
                }
                throw Exception("Category not found")
            },
            onSuccess = { expenseCategory -> onSuccess(expenseCategory) },
            onFailure = { onFailure(SuccessResponse(false, "Category not found")) }
        )
    }


    // Helper function to convert cursor data into ExpenseCategory
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("Range")
    private fun cursor(cursor: Cursor): ExpenseCategory {
        val expenseCategory = ExpenseCategory(cursor.getLong(cursor.getColumnIndex("id"))).apply{
            setName(cursor.getString(cursor.getColumnIndex("name")))
            if(cursor.getColumnIndex("total")>=0){
                setTotal(cursor.getDouble(cursor.getColumnIndex("total")))
            }


        }

        return expenseCategory
    }

    // Function to retrieve all ExpenseCategories from the database (synchronous)
    @RequiresApi(Build.VERSION_CODES.O)
    fun getAll(): List<ExpenseCategory>? {
        val expenseList: MutableList<ExpenseCategory> = ArrayList()
        val cursor = database!!.query("ExpenseCategory", null, null, null, null, null, null)
        cursor?.let {
            it.moveToFirst()
            while (!it.isAfterLast) {
                expenseList.add(cursor(it))
                it.moveToNext()
            }
            it.close()
        }
        return expenseList
    }
}
