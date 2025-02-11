package com.example.wallettracker.data.expenseCategory

import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.BaseDAO
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expense.Expense
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseCategoryDAO(token: String, userId: Int): BaseDAO(token, userId) {

    fun getExpenseCategories(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expenseCategory.getExpenseCategories("Bearer $token").enqueue(object : Callback<List<ExpenseCategoryResponse>> {
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
                    } ?: onFailure(SuccessResponse(success = false, message = response.message()))
                } else {
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<List<ExpenseCategoryResponse>>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    fun getExpenseCategoryById(
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", catId).enqueue(object : Callback<ExpenseCategoryResponse> {
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
                    onFailure(SuccessResponse(success = false, message = response.message()))
                }
            }

            override fun onFailure(call: Call<ExpenseCategoryResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun createExpenseCategories(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }
        val categoryRequest = ExpenseCategoryRequest(category)
        ApiCall.expenseCategory.createExpenseCategories("Bearer $token", categoryRequest).enqueue(object : Callback<ExpenseCategoryResponse> {
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
                        onFailure(SuccessResponse(success = false, message = response.message()))
                    }
                }

            }
            override fun onFailure(call: Call<ExpenseCategoryResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    fun deleteById(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }

        ApiCall.expenseCategory.deleteById("Bearer $token", catId).enqueue(object : Callback<SuccessResponse> {
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
    fun editName(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        category: ExpenseCategory
    ) {
        if (token == null) {
            onFailure(SuccessResponse(success = false, message = "Token not available, login first"))
            return
        }
        val categoryRequest = ExpenseCategoryRequest(category)
        ApiCall.expenseCategory.editName("Bearer $token", categoryRequest).enqueue(object : Callback<SuccessResponse> {
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
                val response = SuccessResponse(false, t.message.toString())
                onFailure(SuccessResponse(success = false, message = response.message))
            }
        })
    }
}
