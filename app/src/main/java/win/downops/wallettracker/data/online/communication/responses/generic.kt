package win.downops.wallettracker.data.online.communication.responses

import win.downops.wallettracker.data.online.communication.requests.CipheredRequest

data class CipheredResponse(
    val signature: String?,
    val encrypted_data: CipheredRequest?
)

data class BaseResponse<T>(
    val data: T?,
    val message: String,
    val success: Boolean
)