package com.example.wallettracker.data.expenseCategory

data class ExpenseCategoryResponse(
    val id: Long,
    val name: String,
    val total: Double
)

class ExpenseCategory {
    private var id: Long = 0
    private var name: String = ""
    private var total: Double = 0.0
    constructor()
    constructor(name: String) {
        this.name = name
    }
    constructor(id:Long, name: String) {
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
    fun setName(value: String){
        this.name = value
    }
    fun getTotal(): Double{
        return this.total
    }
    fun setTotal(value: Double){
        this.total = value
    }


}

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

