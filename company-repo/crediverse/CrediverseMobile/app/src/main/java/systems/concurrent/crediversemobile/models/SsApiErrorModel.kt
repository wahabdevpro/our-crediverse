package systems.concurrent.crediversemobile.models

data class SsApiError(
    val code: Int,
    val message: String,
)

data class SsApiErrorModel(
    val error: SsApiError?,
    val originTransactionId: String?,
    val traceId: String?,
)