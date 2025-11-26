package systems.concurrent.crediversemobile.models

enum class MobileMoneyDepositStatus {
    SUCCESS,
    FAILED,
    UNAUTHORIZED;
}

data class MobileMoneyDepositResponse(
    var status: MobileMoneyDepositStatus
)
