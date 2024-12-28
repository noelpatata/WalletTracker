package com.example.wallettracker.data.Expense


class ExpenseCategory {
    private val id: Long = 0
    private var name: String = ""
    private var total: Double = 0.0
    constructor()
    constructor(name: String) {
        this.name = name
    }
    fun getId(): Long{
        return this.id
    }
    fun getName(): String{
        return this.name
    }
    fun getTotal(): Double{
        return this.total
    }
    fun setTotal(value: Double){
        this.total = value
    }
    fun setName(value: String){
        this.name = value
    }

}