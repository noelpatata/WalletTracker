package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import BaseDAO
import android.content.Context
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategory
import com.example.wallettracker.util.Constantes.authenticationErrorMessage
import com.example.wallettracker.util.Constantes.noDataMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseDAO(context: Context): BaseDAO<Expense>(context)  {

    fun getById(
        onSuccess: (Expense) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        expenseId: Long
    ) {

        ApiCall.expense.getById("Bearer $token", cipheredText, expenseId).enqueue(object : Callback<DataResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {
                if (response.isSuccessful) {
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        val jsonData = verifyData(data)
                        if(jsonData.isEmpty()){
                            onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
                        }
                        else{
                            val expenseCategories =  singlemap(jsonData)
                            if (expenseCategories != null) {
                                onSuccess(expenseCategories)
                            }
                        }

                    }
                    else{
                        onFailure(SuccessResponse(success = false, message = noDataMessage))
                    }

                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }

    fun getByCatId(
        onSuccess: (List<Expense>) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {

        ApiCall.expense.getByCatId("Bearer $token", cipheredText, catId).enqueue(object : Callback<DataResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {
                if (response.isSuccessful) {
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        val jsonData = verifyData(data)
                        if(jsonData.isEmpty()){
                            onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
                        }
                        else{
                            val expenseCategories =  map(jsonData)
                            onSuccess(expenseCategories)
                        }

                    }
                    else{
                        onFailure(SuccessResponse(success = false, message = noDataMessage))
                    }

                }
            }

            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun deleteAll(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {

        ApiCall.expense.deleteAll("Bearer $token", cipheredText).enqueue(object : Callback<SuccessResponse> {
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

        ApiCall.expense.deleteById("Bearer $token", cipheredText, expenseId).enqueue(object : Callback<SuccessResponse> {
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
        onSuccess: (Expense) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        val expenseRequest = ExpenseRequest(expense, userId)
        ApiCall.expense.createExpense("Bearer $token", cipheredText, expenseRequest).enqueue(object : Callback<DataResponse> {
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {
                if (response.isSuccessful) {
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        val jsonData = verifyData(data)
                        if(jsonData.isEmpty()){
                            onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
                        }
                        else{
                            val expenseCategories =  singlemap(jsonData)
                            if (expenseCategories != null) {
                                onSuccess(expenseCategories)
                            }
                        }

                    }
                    else{
                        onFailure(SuccessResponse(success = false, message = noDataMessage))
                    }

                }


            }
            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun edit(
        expense: Expense,
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        val expenseRequest = ExpenseRequest(expense, userId)
        ApiCall.expense.edit("Bearer $token", cipheredText, expenseRequest).enqueue(object : Callback<SuccessResponse> {
            override fun onResponse(
                call: Call<SuccessResponse>,
                response: Response<SuccessResponse>
            ) {
                if(response.isSuccessful){
                    onFailure(response.body()!!)
                }


            }
            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun singlemap(jsonData: String): Expense? {
        return try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<Expense>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun map(jsonData: String): List<Expense> {
        return try {
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<Expense>>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
