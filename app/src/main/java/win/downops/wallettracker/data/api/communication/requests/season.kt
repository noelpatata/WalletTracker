package win.downops.wallettracker.data.api.communication.requests

data class SeasonIdRequest(val id: Long)
data class GetOrCreateSeasonRequest(val year: Int, val month: Int)
