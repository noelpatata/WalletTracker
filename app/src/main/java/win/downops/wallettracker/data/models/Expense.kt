package win.downops.wallettracker.data.models

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class Expense {
    private var id: Long = 0
    private var price: Double? = null
    private var description: String = ""
    private var expenseDate: Date? = null
    private var category: Long? = null
    constructor()
    constructor(id:Long, price: Double?, expenseDate: Date?, category: Long?, description: String) {
        this.id = id
        this.price = price
        this.expenseDate = expenseDate
        this.category = category
        this.description = description
    }
    constructor(price: Double?, expenseDate: Date?, category: Long?, description: String) {
        this.price = price
        this.expenseDate = expenseDate
        this.category = category
        this.description = description

    }
    constructor(id: Long) {
        this.id = id
    }
    fun getId(): Long{
        return this.id
    }
    fun getDescription(): String{
        return this.description
    }
    fun setDescription(value: String){
        this.description = value
    }

    fun getPrice(): Double{
        return this.price!!
    }
    fun setPrice(value: Double){
        this.price = value
    }
    fun getDate(): Date {
        return this.expenseDate!!
    }
    fun getDateString(): String{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return  dateFormat.format(this.getDate())
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