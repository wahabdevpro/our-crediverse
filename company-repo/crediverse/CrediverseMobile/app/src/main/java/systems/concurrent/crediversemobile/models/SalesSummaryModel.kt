package systems.concurrent.crediversemobile.models

open class SalesSummaryValue(
    open val airtimeSalesValue: String,
    open val airtimeSalesCount: Long,
    open val airtimeCostOfGoodsSold: String,
    open val airtimeUnknownCostCount: Long,

    open val bundleSalesValue: String,
    open val bundleSalesCount: Long,
    open val bundleCostOfGoodsSold: String,
    open val bundleUnknownCostCount: Long,

    open val tradeBonusValue: String,
    open val inboundTransfersValue: String,
    open val inboundTransfersCount: Long,

    var stockLevel: String? = null
)

class HourlySalesSummaryValue(
    val date: Long,
    val hour: Long,
    override val airtimeSalesValue: String,
    override val airtimeSalesCount: Long,
    override val bundleSalesValue: String,
    override val bundleSalesCount: Long,
) : SalesSummaryValue(
    /**
     * We do not use the COGS Values and UNKNOWN Count in hourly sales -- so they are set to defaults
     */
    airtimeSalesValue, airtimeSalesCount, "0.0000", 0,
    bundleSalesValue, bundleSalesCount, "0.0000", 0, "0.0000",
    "0.0000", 0
)

data class HourlySalesSummaryModel(
    val startTime: Long,
    val endTime: Long,
    val value: List<HourlySalesSummaryValue>
)

data class SalesSummaryModel(
    val startTime: Long,
    val endTime: Long,
    val value: SalesSummaryValue
)