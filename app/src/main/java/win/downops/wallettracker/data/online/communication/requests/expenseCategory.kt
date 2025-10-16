package win.downops.wallettracker.data.online.communication.requests

data class ExpenseCategoryIdRequest(
    val id: Long
)
data class CreateExpenseCategoryRequest(
    val name: String
)
data class EditExpenseCategoryRequest(
    val id: Long,
    val name: String
)
