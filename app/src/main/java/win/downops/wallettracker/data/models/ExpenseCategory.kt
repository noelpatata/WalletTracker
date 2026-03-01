package win.downops.wallettracker.data.models

class ExpenseCategory {
    private var id: Long = 0
    private var name: String = ""
    private var total: Double = 0.0
    private var sortOrder: Int? = null
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
    fun getOrder(): Int?{
        return this.sortOrder
    }
    fun setOrder(value: Int?){
        this.sortOrder = value
    }
    fun getTotal(): Double{
        return this.total
    }
    fun setTotal(value: Double){
        this.total = value
    }
    override fun toString(): String {
        return "ExpenseCategory(id=$id, name=$name, total=$total)"
    }

}


