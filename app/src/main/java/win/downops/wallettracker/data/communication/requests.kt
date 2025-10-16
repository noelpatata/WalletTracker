package win.downops.wallettracker.data.communication


data class CipheredRequest(
    val encrypted_aes_key: String?,
    val iv: String?,
    val ciphertext: String?,
    val tag: String?
)
data class ExpenseCategoryIdRequest(
    val id: Long
)
data class ExpenseByCategoryIdRequest(
    val catId: Long
)
data class ExpenseIdRequest(
    val id: Long
)
