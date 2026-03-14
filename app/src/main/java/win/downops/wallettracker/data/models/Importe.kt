package win.downops.wallettracker.data.models

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.Locale

class Importe {
    private var id: Long = 0
    private var concept: String = ""
    private var importeDate: Date? = null
    private var amount: Double = 0.0
    private var balanceAfter: Double = 0.0
    private var seasonId: Long = 0

    constructor()
    constructor(id: Long, concept: String, importeDate: Date?, amount: Double, balanceAfter: Double, seasonId: Long) {
        this.id = id
        this.concept = concept
        this.importeDate = importeDate
        this.amount = amount
        this.balanceAfter = balanceAfter
        this.seasonId = seasonId
    }
    constructor(concept: String, importeDate: Date?, amount: Double, balanceAfter: Double, seasonId: Long) {
        this.concept = concept
        this.importeDate = importeDate
        this.amount = amount
        this.balanceAfter = balanceAfter
        this.seasonId = seasonId
    }
    constructor(id: Long) {
        this.id = id
    }

    fun getId(): Long = id
    fun getConcept(): String = concept
    fun setConcept(value: String) { concept = value }
    fun getDate(): Date = importeDate!!
    fun getDateString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(importeDate)
    fun setDate(value: Date) { importeDate = value }
    fun getAmount(): Double = amount
    fun setAmount(value: Double) { amount = value }
    fun getBalanceAfter(): Double = balanceAfter
    fun setBalanceAfter(value: Double) { balanceAfter = value }
    fun getSeasonId(): Long = seasonId
    fun setSeasonId(value: Long) { seasonId = value }
}
