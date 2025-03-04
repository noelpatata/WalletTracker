package com.example.wallettracker.data.expense

import android.provider.ContactsContract.Data
import com.example.wallettracker.data.CatIdRequest
import com.example.wallettracker.data.DataResponse
import com.example.wallettracker.data.ExpenseIdRequest
import com.example.wallettracker.data.SuccessResponse
import com.example.wallettracker.data.SymmetricResponse
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryRequest
import com.example.wallettracker.data.expenseCategory.ExpenseCategoryResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Header
import retrofit2.http.POST

interface ExpenseEPs {


    @DELETE("Expense/all/")
    fun deleteAll(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
    ): Call<SuccessResponse>

    @POST("Expense/CatId/")
    fun getByCatId(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body catId: SymmetricResponse
    ): Call<DataResponse>

    @POST("Expense/edit/")
    fun edit(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseCategory: SymmetricResponse
    ): Call<DataResponse>

    @POST("Expense/create/")
    fun createExpense(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expense: SymmetricResponse
    ): Call<DataResponse>

    @POST("Expense/Id")
    fun getById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId: SymmetricResponse
    ): Call<DataResponse>

    @POST("Expense/delete/")
    fun deleteById(
        @Header("Authorization") token: String,
        @Header("Cipher") cipher: String,
        @Body expenseId : SymmetricResponse
    ): Call<SuccessResponse>
}