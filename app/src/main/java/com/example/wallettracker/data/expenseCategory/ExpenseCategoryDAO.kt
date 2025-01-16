package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.BaseDAO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseCategoryDAO(token: String, userId: Int): BaseDAO(token, userId) {

    fun getExpenseCategories(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }

        ApiCall.expenseCategory.getExpenseCategories("Bearer $token", userId).enqueue(object : Callback<List<ExpenseCategoryResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseCategoryResponse>>,
                response: Response<List<ExpenseCategoryResponse>>
            ) {
                if (response.isSuccessful) {
                    val expenseCategories = response.body()?.map { responseItem ->
                        ExpenseCategory(responseItem.id).apply {
                            setName(responseItem.name)
                            setTotal(responseItem.total)
                        }
                    }
                    expenseCategories?.let {
                        onSuccess(it)
                    } ?: onFailure("No data received")
                } else {
                    onFailure("Failed to fetch categories: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ExpenseCategoryResponse>>, t: Throwable) {
                onFailure("Failed to fetch categories: ${t.message}")
            }
        })
    }

    fun getExpenseCategoryById(
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (String) -> Unit,
        catId: Long
    ) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }

        ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", userId, catId).enqueue(object : Callback<ExpenseCategoryResponse> {
            override fun onResponse(
                call: Call<ExpenseCategoryResponse>,
                response: Response<ExpenseCategoryResponse>
            ) {
                if (response.isSuccessful) {
                    var resp = response.body()!!
                    val category = ExpenseCategory(resp.id, resp.name)
                    category.let {
                        onSuccess(it)
                    }
                } else {
                    onFailure("Failed to fetch category: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ExpenseCategoryResponse>, t: Throwable) {
                onFailure("Failed to fetch category: ${t.message}")
            }
        })
    }
    fun createExpenseCategories(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }
        val categoryRequest = ExpenseCategoryRequest(category.getName())
        ApiCall.expenseCategory.createExpenseCategories("Bearer $token", userId, categoryRequest).enqueue(object : Callback<ExpenseCategoryResponse> {
            override fun onResponse(
                call: Call<ExpenseCategoryResponse>,
                response: Response<ExpenseCategoryResponse>
            ) {
                if (response.isSuccessful) {
                    val catFromResponse = response.body()
                    if (catFromResponse != null) {
                        val expenseCategory = ExpenseCategory(catFromResponse.id).apply {
                            setName(catFromResponse.name)
                            setTotal(catFromResponse.total)
                        }
                        expenseCategory.let {
                            onSuccess(it)
                        }
                    } else {
                        onFailure("Failed to create category: ${response.message()}")
                    }
                }

            }
            override fun onFailure(call: Call<ExpenseCategoryResponse>, t: Throwable) {
                onFailure("Failed to create category: ${t.message}")
            }
        })
    }
}
