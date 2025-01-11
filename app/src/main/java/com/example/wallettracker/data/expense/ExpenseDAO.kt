package com.example.wallettracker.data.expense

import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.BaseDAO
import com.example.wallettracker.data.SuccessResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpenseDAO(token: String, userId: Int): BaseDAO(token, userId) {

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
