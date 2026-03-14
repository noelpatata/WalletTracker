package win.downops.wallettracker.data.models

class Season {
    private var id: Long = 0
    private var year: Int = 0
    private var month: Int = 0

    constructor()
    constructor(id: Long, year: Int, month: Int) {
        this.id = id
        this.year = year
        this.month = month
    }
    constructor(year: Int, month: Int) {
        this.year = year
        this.month = month
    }

    fun getId(): Long = id
    fun getYear(): Int = year
    fun getMonth(): Int = month
    fun setYear(value: Int) { year = value }
    fun setMonth(value: Int) { month = value }
}
