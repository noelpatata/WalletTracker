package com.example.wallettracker.data.expense

import java.sql.Date


class Expense {
    private var id: Long = 0
    private var price: Double? = null
    private var expenseDate: Date? = null
    private var category: Long? = null
    private var userId: Long? = null
    constructor()
    constructor(id:Long, price: Double?, expenseDate: Date?, category: Long?) {
        this.id = id
        this.price = price
        this.expenseDate = expenseDate
        this.category = category
    }
    constructor(price: Double?, expenseDate: Date?, category: Long?) {
        this.price = price
        this.expenseDate = expenseDate
        this.category = category
    }
    constructor(id: Long) {
        this.id = id
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
    fun getUserId(): Long{
        return this.userId!!
    }

}