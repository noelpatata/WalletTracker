package com.example.wallettracker.data.expense


class ExpenseCategory {
    private var id: Long = 0
    private var name: String = ""
    private var total: Double = 0.0
    constructor()
    constructor(name: String) {
        this.name = name
    }
    constructor(id: Long) {
        this.id = id
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