package com.example.wallettracker.data.expenseCategory

import BaseDAO
import Cryptography
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.util.Constantes.authenticationErrorMessage
import com.example.wallettracker.util.Constantes.noDataMessage

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseCategoryDAO(context: Context): BaseDAO<ExpenseCategory>(context) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun getExpenseCategories(
        onSuccess: (List<ExpenseCategory>) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        ApiCall.expenseCategory.getExpenseCategories("Bearer $token", cipheredText).enqueue(object : Callback<DataResponse> {
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



    fun getExpenseCategoryById(
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {
        ApiCall.expenseCategory.getExpenseCategoryById("Bearer $token", cipheredText, CatIdRequest(catId)).enqueue(object : Callback<DataResponse> {
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {
                if (response.isSuccessful) {
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        val jsonData = verifyData(data)
                        if(jsonData == "error"){
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
    fun createExpenseCategories(
        category: ExpenseCategory,
        onSuccess: (ExpenseCategory) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        val categoryRequest = ExpenseCategoryRequest(category)
        ApiCall.expenseCategory.createExpenseCategories("Bearer $token", cipheredText, categoryRequest).enqueue(object : Callback<DataResponse> {
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

    fun deleteById(
        onSuccess: (SuccessResponse) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        catId: Long
    ) {

        ApiCall.expenseCategory.deleteById("Bearer $token", cipheredText, CatIdRequest(catId)).enqueue(object : Callback<SuccessResponse> {
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
                    onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
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
        val categoryRequest = ExpenseCategoryRequest(category)
        ApiCall.expenseCategory.editName("Bearer $token", cipheredText, categoryRequest).enqueue(object : Callback<SuccessResponse> {
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
                    onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
                }
            }

            override fun onFailure(call: Call<SuccessResponse>, t: Throwable) {
                val response = SuccessResponse(false, t.message.toString())
                onFailure(SuccessResponse(success = false, message = response.message))
            }
        })
    }
    fun singlemap(jsonData: String): ExpenseCategory? {
        return try {
            if(jsonData == "{}") return null
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<ExpenseCategory>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun map(jsonData: String): List<ExpenseCategory> {
        return try {
            if(jsonData == "[]") return emptyList()
            val gson = com.google.gson.Gson()
            val type = object : com.google.gson.reflect.TypeToken<List<ExpenseCategory>>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


}
