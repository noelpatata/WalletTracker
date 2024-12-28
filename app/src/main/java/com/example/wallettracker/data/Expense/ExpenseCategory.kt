package com.example.wallettracker.data.ExpenseCategory

import java.sql.Date


class ExpenseCategory {
    private val id: Long = 0
    private var name: String = ""
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
    fun setName(value: String){
        this.name = value
    }
}