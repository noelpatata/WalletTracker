package com.example.wallettracker.data

import com.example.wallettracker.data.expense.OnlineExpenseEPs
import com.example.wallettracker.data.expenseCategory.OnlineExpenseCategoryEPs
import com.example.wallettracker.data.login.LoginEPs
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiCall {
    //TODO take base url from env variables
    private const val BASE_URL = "http://172.20.0.190:8080"


    val login: LoginEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LoginEPs::class.java)
    }

    val expenseCategory: OnlineExpenseCategoryEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OnlineExpenseCategoryEPs::class.java)
    }

    val expense: OnlineExpenseEPs by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OnlineExpenseEPs::class.java)
    }


}