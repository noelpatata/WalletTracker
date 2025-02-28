package com.example.wallettracker.data.expense

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.wallettracker.data.ApiCall
import BaseDAO
import android.content.Context
import android.util.Log
import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.ExpenseIdRequest
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.util.Constantes.authenticationErrorMessage
import com.example.wallettracker.util.Constantes.fetchingDataMessage
import com.example.wallettracker.util.Constantes.noDataMessage
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import kotlin.math.exp

@RequiresApi(Build.VERSION_CODES.O)
class ExpenseDAO(context: Context): BaseDAO<Expense>(context)  {

    fun getById(
        onSuccess: (Expense) -> Unit,
        onFailure: (SuccessResponse) -> Unit,
        expenseId: Long
    ) {

        ApiCall.expense.getById("Bearer $token", cipheredText, ExpenseIdRequest(expenseId)).enqueue(object : Callback<DataResponse> {
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

        ApiCall.expense.getByCatId("Bearer $token", cipheredText, CatIdRequest(catId)).enqueue(object : Callback<DataResponse> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {

                if (response.isSuccessful) {
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        try{
                            val jsonData = verifyData(data)
                            val expenseCategories = map(jsonData)
                            onSuccess(expenseCategories)

                        }
                        catch(ex: Exception){
                            onFailure(SuccessResponse(success = false, message = "aqui:"+ex.message.toString()))
                        }


                    }
                    else{
                        onFailure(SuccessResponse(success = false, message = noDataMessage))
                    }
                }else{
                    onFailure(SuccessResponse(success = false, message = response.message()))
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

        ApiCall.expense.deleteById("Bearer $token", cipheredText, ExpenseIdRequest(expenseId)).enqueue(object : Callback<SuccessResponse> {
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
        onSuccess: (Expense) -> Unit,
        onFailure: (SuccessResponse) -> Unit
    ) {
        val expenseRequest = ExpenseRequest(expense, userId)
        ApiCall.expense.edit("Bearer $token", cipheredText, expenseRequest).enqueue(object : Callback<DataResponse> {
            override fun onResponse(
                call: Call<DataResponse>,
                response: Response<DataResponse>
            ) {
                if(response.isSuccessful){
                    val data: DataResponse? = response.body()
                    if (data != null) {
                        val jsonData = verifyData(data)
                        if(jsonData == ""){
                            onFailure(SuccessResponse(success = false, message = authenticationErrorMessage))
                        }
                        else{
                            val edittedExpense =  singlemap(jsonData)
                            if (edittedExpense != null) {
                                onSuccess(edittedExpense)
                            }
                        }

                    }
                    else{
                        onFailure(SuccessResponse(success = false, message = fetchingDataMessage))
                    }
                }
                else{
                    onFailure(SuccessResponse(success = false, message = noDataMessage))
                }



            }
            override fun onFailure(call: Call<DataResponse>, t: Throwable) {
                onFailure(SuccessResponse(success = false, message = t.message.toString()))
            }
        })
    }
    fun singlemap(jsonData: String): Expense? {
        return try {
            if(jsonData == "{}") return null
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Register the custom deserializer
                .create()
            val type = object : com.google.gson.reflect.TypeToken<Expense>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun map(jsonData: String): List<Expense> {
        return try {
            if(jsonData == "[]") return emptyList()
            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd") // Register the custom deserializer
                .create()

            val type = object : com.google.gson.reflect.TypeToken<List<Expense>>() {}.type
            gson.fromJson(jsonData, type)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
