package win.downops.wallettracker.data.api.communication.requests


data class ExpenseByCategoryIdRequest(
    val catId: Long
)
data class ExpenseIdRequest(
    val id: Long
)
data class CreateExpenseRequest(
    val price: Double,
    val expenseDate: String,
    val category: Long,
    val description: String
)
data class EditExpenseRequest(
    val id: Long,
    val price: Double,
    val expenseDate: String,
    val category: Long,
    val description: String
)
