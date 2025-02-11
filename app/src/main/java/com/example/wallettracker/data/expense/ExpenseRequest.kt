package com.example.wallettracker.data.expense

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ExpenseRequest() {
    private var id: Long = -1
    private var price: Double = -1.0
    private lateinit var expenseDate: String
    private var category: Long = -1
    private var user: Int = -1
    constructor(exp: Expense, userId: Int) : this() {
        this.id = exp.getId()
        this.price = exp.getPrice()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        this.expenseDate = dateFormat.format(exp.getDate())
        this.category = exp.getCategoryId()
        this.user = userId
    }
}