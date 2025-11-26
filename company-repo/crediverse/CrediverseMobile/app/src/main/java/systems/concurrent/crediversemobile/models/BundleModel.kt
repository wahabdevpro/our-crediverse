package systems.concurrent.crediversemobile.models

import java.util.Date

enum class BundleType {
    DEFAULT;
}

data class Error(
    val code: String,
    val message: String
)

data class Disallowed(
    val code: String,
    val message: String // message like "can't provision, already subscribed"
)

data class Charge(
    val amount: Double,
    val currency: String
)

data class Period(
    val seconds: Long,
    val startDate: Date, // will be relative, won't likely use start/end
    val endDate: Date,
)

enum class AutoRenew {
    ON,
    OFF,
    AUTO; // subscriber chooses (only available on next iteration - maybe)
}

data class LifeCycle(
    val period: Period,
    val limit: Limit?,
    val autoRenew: AutoRenew?
)

enum class MethodCode {
    PROVISION, // once off
    SUBSCRIBE, // ongoing
    EXTEND, // Renew existing provision
    CANCEL;
}

data class Method(
    val code: String,
    val charge: Charge,
    val lifecycle: LifeCycle?,
    val disallowed: Disallowed?
)

data class Limit(
    val seconds: Long,
)

data class Benefit(
    val code: String,
    val name: String,
    val value: Long,
    val units: String,
)

data class BundleModel(
    val code: String,
    val name: String,
    val categoryCodes: List<String>?,
    val methods: List<Method>,
    val benefits: List<Benefit>,
)

data class BundleCategory(
    val code: String,
    val name: String,
    val disallowed: Boolean,
    val bundle_codes: List<String>?,
    val categories: List<BundleCategory>?,
)

data class SmartshopBundlesListGet(
    val traceId: String,
    val categories: List<BundleCategory>,
    val bundles: List<BundleModel>
)

data class SellBundleOutcome(
    val method: Method, // will not contain disallowed
    val bundle: BundleModel
)

data class SmartshopSellBundleResponse(
    val traceId: String,
    val transactionId: String,
    val originTransactionId: String,
    val outcome: SellBundleOutcome,
)

interface OptionParameter {
    val autoRenew: AutoRenew?
}

data class CustomParameters(
    val key: String,
    val value: String
)

data class SmartshopSellBundleRequestProperties(
    val originTransactionId: String?,
    val originChannel: String?,
    val customParameters: List<CustomParameters>?,
    val geoCoordinates: SsApiCoordinatesModel?,
    val options: OptionParameter?,
)