package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.BaseDAO
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRequest
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.format.DateTimeFormatter

class ExpenseDAO(token: String, userId: Int): BaseDAO(token, userId) {

    fun getById(
        onSuccess: (Expense) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        expenseId: Long
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expense.getById("Bearer $token", expenseId).enqueue(object : Callback<ExpenseResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<ExpenseResponse>,
                response: Response<ExpenseResponse>
            ) {
                if (response.isSuccessful) {
                    val responseItem = response.body()
                    if (responseItem != null) {
                        val exp =Expense(responseItem.id).apply {
                            setPrice(responseItem.price)
                            setDate(Date.valueOf(responseItem.expenseDate))
                            setCategoryId(responseItem.category)
                        }
                        onSuccess(exp)
                    }

                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<ExpenseResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    fun getByCatId(
        onSuccess: (List<Expense>) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expense.getByCatId("Bearer $token", catId).enqueue(object : Callback<List<ExpenseResponse>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<List<ExpenseResponse>>,
                response: Response<List<ExpenseResponse>>
            ) {
                if (response.isSuccessful) {
                    val expenseCategories = response.body()?.map { responseItem ->
                        Expense(responseItem.id).apply {
                            setPrice(responseItem.price)
                            setDate(Date.valueOf(responseItem.expenseDate))
                            setCategoryId(responseItem.category)
                        }
                    }
                    expenseCategories?.let {
                        onSuccess(it)
                    }
                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun deleteAll(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expense.deleteAll("Bearer $token").enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    val success = response.body()
                    success?.let {
                        onSuccess(it)
                    } ?: onFailure(SuccessResponse(success = false, message = response.message()))
                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    fun deleteById(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        expenseId: Long
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Error deleting expense"))
            return
        }

        ApiCall.expense.deleteById("Bearer $token", expenseId).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    val success = response.body()
                    success?.let {
                        onSuccess(it)
                    } ?: onFailure(SuccessResponse(success = false, message = response.message()))
                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun createExpense(
        expense: Expense,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }
        val expenseRequest = ExpenseRequest(expense, userId)
        ApiCall.expense.createExpense("Bearer $token", expenseRequest).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if(response.isSuccessful){
                    if (response.code() == 200) {
                        onSuccess(response.body()!!)
                    }
                    else{
                        onFailure(response.body()!!)
                    }
                }


            }
            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun edit(
        expense: Expense,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }
        val expenseRequest = ExpenseRequest(expense, userId)
        ApiCall.expense.edit("Bearer $token", expenseRequest).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if(response.isSuccessful){
                    if (response.code() == 200) {
                        onSuccess(response.body()!!)
                    }
                    else{
                        onFailure(response.body()!!)
                    }
                }


            }
            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
}
