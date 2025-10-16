package win.downops.wallettracker.data.expense

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class Expense {
    private var id: Long = 0
    private var price: Double? = null
    private var description: String = ""
    private var expenseDate: Date? = null
    private var category: Long? = null
    private var userId: Long? = null
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
data class ExpenseResponse(
    val category: Long,
    val expenseDate: String,
    val id: Long,
    val price: Double,
    val user: Int
)