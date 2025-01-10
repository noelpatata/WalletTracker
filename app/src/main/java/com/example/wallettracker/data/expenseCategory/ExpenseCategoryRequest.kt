package com.example.wallettracker.data.expenseCategory


class ExpenseCategoryRequest() {
    private lateinit var name: String
    constructor(name: String) : this() {
        this.name = name
    }

    fun getName(): String{
        return this.name
    }
    fun setName(value: String){
        this.name = value
    }
}