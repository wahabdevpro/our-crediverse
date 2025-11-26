package systems.concurrent.crediversemobile.models

import systems.concurrent.masapi.MasApi

data class SalesTargetModel(
    val msisdn: String,
    val period: MasApi.Period,
    val targetAmount: String?
)
