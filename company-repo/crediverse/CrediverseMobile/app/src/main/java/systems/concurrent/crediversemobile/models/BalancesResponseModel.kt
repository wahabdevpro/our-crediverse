package systems.concurrent.crediversemobile.models

import systems.concurrent.crediversemobile.utils.CustomUtils.Companion.nowEpoch

data class BalancesResponseModel(
    val balance: String,
    val bonusBalance: String,
    val onHoldBalance: String,
) {
    val lastUpdated: Long = nowEpoch()
    val totalBalance = (balance.toDoubleOrNull() ?: 0.0) + (bonusBalance.toDoubleOrNull() ?: 0.0)
}
