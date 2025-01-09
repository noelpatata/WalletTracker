package com.example.wallettracker.data.expenseCategory

import android.util.Base64
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.LoginRequest
import com.example.wallettracker.data.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseCategoryDAO(private val credentials: LoginRequest) {
    private var token: String? = null

    fun login(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credentials = "${credentials.username}:${credentials.password}"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        ApiCall.expenseCategory.login(basicAuth).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    token = response.body()?.token
                    onSuccess()
                } else {
                    onFailure("Login failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                onFailure("${t.message}")
            }
        })
    }


    fun getExpenseCategories(
        userId: Int,
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
}
