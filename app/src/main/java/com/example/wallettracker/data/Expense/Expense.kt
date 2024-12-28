package com.example.wallettracker.data.ExpenseCategory

import java.sql.Date


class Expense {
    private val id: Long = 0
    private var price: Double? = null
    private var expenseDate: Date? = null
    private var category: Long? = null
    constructor()
    constructor(price: Double?, expenseDate: Date?, category: Long?) {
        this.price = price
        this.expenseDate = expenseDate
        this.category = category
    }
    fun getId(): Long{
        return this.id
    }

    fun getPrice(): Double{
        return this.price!!
    }
    fun setPrice(value: Double){
        this.price = value
    }
    fun getDate(): Date{
        return this.expenseDate!!
    }
    fun setDate(value: Date){
        this.expenseDate = value
    }
    fun getCategoryId(): Long{
        return this.category!!
    }
    fun setCategoryId(value: Long){
        this.category = value
    }
}