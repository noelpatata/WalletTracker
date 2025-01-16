package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.BaseDAO
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.sql.Date
import java.time.format.DateTimeFormatter

class ExpenseDAO(token: String, userId: Int): BaseDAO(token, userId) {

    fun getById(
        onSuccess: (List<Expense>) -> Unit,
        onFailure: (String) -> Unit,
        catId: Long
    ) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }

        ApiCall.expense.getByCatId("Bearer $token", userId, catId).enqueue(object : Callback<List<ExpenseResponse>> {
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
                    onFailure("Failed to fetch category: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                onFailure("Failed to fetch category: ${t.message}")
            }
        })
    }
    fun deleteAll(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }

        ApiCall.expense.deleteAll("Bearer $token", userId).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if (response.isSuccessful) {
                    val success = response.body()
                    success?.let {
                        onSuccess(it)
                    } ?: onFailure("Failed deleting expenses")
                } else {
                    onFailure(response.message())
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(t.message.toString())
            }
        })
    }
}
