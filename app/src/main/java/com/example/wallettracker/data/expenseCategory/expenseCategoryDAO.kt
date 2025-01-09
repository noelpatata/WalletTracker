package com.example.wallettracker.data.expenseCategory

import android.util.Base64
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.LoginRequest
import com.example.wallettracker.data.LoginResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class expenseCategoryDAO(private val username: String, private val password: String) {
    private var token: String? = null

    // Login and retrieve the token
    fun login(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credentials = "$username:$password"
        val basicAuth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

        ApiCall.api.login(basicAuth).enqueue(object : Callback<LoginResponse> {
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

    // Fetch expense categories for a specific user
    fun getExpenseCategories(userId: Int, onSuccess: (List<ExpenseCategory>) -> Unit, onFailure: (String) -> Unit) {
        if (token == null) {
            onFailure("Token not available. Login first.")
            return
        }

        ApiCall.api.getExpenseCategories("Bearer $token", userId).enqueue(object : Callback<List<ExpenseCategory>> {
            override fun onResponse(
                call: Call<List<ExpenseCategory>>,
                response: Response<List<ExpenseCategory>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let { onSuccess(it) } ?: onFailure("No data received")
                } else {
                    onFailure("Failed to fetch categories: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<List<ExpenseCategory>>, t: Throwable) {
                onFailure("Failed to fetch categories: ${t.message}")
            }
        })
    }
}
