package com.example.wallettracker.data.expenseCategory


class ExpenseCategoryRequest() {
    private var id: Long = -1
    private lateinit var name: String

    constructor(category: ExpenseCategory) : this() {
        this.id = category.getId()
        this.name = category.getName()
    }

    fun getName(): String{
        return this.name
    }
    fun setName(value: String){
        this.name = value
    }
    fun setId(value: Long){
        this.id = value
    }
}