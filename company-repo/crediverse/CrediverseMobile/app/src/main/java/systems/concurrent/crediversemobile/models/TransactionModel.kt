package systems.concurrent.crediversemobile.models

import systems.concurrent.crediversemobile.R

enum class TXStatus(private val _saleStatusResource: Int) {
    ALREADY_ADJUDICATED(R.string.ALREADY_REGISTERED),
    ALREADY_REGISTERED(R.string.ALREADY_REGISTERED),
    ALREADY_REVERSED(R.string.ALREADY_REVERSED),
    BUNDLE_SALE_FAILED(R.string.BUNDLE_SALE_FAILED),
    CO_AUTHORIZE(R.string.CO_AUTHORIZE),
    CO_SIGN_ONLY_SESSION(R.string.CO_SIGN_ONLY_SESSION),
    DAY_AMOUNT_LIMIT(R.string.DAY_AMOUNT_LIMIT),
    DAY_COUNT_LIMIT(R.string.DAY_COUNT_LIMIT),
    FORBIDDEN(R.string.auth_forbidden),
    HISTORIC_PASSWORD(R.string.HISTORIC_PASSWORD),
    IMSI_LOCKOUT(R.string.IMSI_LOCKOUT),
    INSUFFICIENT_FUNDS(R.string.INSUFFICIENT_FUNDS),
    INSUFFICIENT_PROVISN(R.string.INSUFFICIENT_PROVISN),
    INTRATIER_TRANSFER(R.string.INTRATIER_TRANSFER),
    INVALID_AGENT(R.string.INVALID_AGENT),
    INVALID_AMOUNT(R.string.INVALID_AMOUNT),
    INVALID_BUNDLE(R.string.INVALID_BUNDLE),
    INVALID_CHANNEL(R.string.INVALID_CHANNEL),
    INVALID_PASSWORD(R.string.INVALID_PASSWORD),
    INVALID_PIN(R.string.INVALID_PIN),
    INVALID_RECIPIENT(R.string.INVALID_RECIPIENT),
    INVALID_STATE(R.string.INVALID_STATE),
    INVALID_TRAN_TYPE(R.string.INVALID_TRAN_TYPE),
    INVALID_VALUE(R.string.INVALID_VALUE),
    MAX_AMOUNT_LIMIT(R.string.MAX_AMOUNT_LIMIT),
    MONTH_AMOUNT_LIMIT(R.string.MONTH_AMOUNT_LIMIT),
    MONTH_COUNT_LIMIT(R.string.MONTH_COUNT_LIMIT),
    NOT_ELIGIBLE(R.string.NOT_ELIGIBLE),
    NOT_REGISTERED(R.string.NOT_REGISTERED),
    NOT_SELF(R.string.NOT_SELF),
    NOT_WEBUSER_SESSION(R.string.NOT_WEBUSER_SESSION),
    NO_IMSI(R.string.NO_IMSI),
    NO_LOCATION(R.string.NO_LOCATION),
    NO_TRANSFER_RULE(R.string.NO_TRANSFER_RULE),
    OTHER_ERROR(R.string.OTHER_ERROR),
    PASSWORD_LOCKOUT(R.string.PASSWORD_LOCKOUT),
    PIN_LOCKOUT(R.string.PIN_LOCKOUT),
    REFILL_BARRED(R.string.REFILL_BARRED),
    REFILL_DENIED(R.string.REFILL_DENIED),
    REFILL_FAILED(R.string.REFILL_FAILED),
    REFILL_NOT_ACCEPTED(R.string.REFILL_NOT_ACCEPTED),
    SESSION_EXPIRED(R.string.SESSION_EXPIRED),
    TECHNICAL_PROBLEM(R.string.TECHNICAL_PROBLEM),
    TEMPORARY_BLOCKED(R.string.TEMPORARY_BLOCKED),
    TIMED_OUT(R.string.TIMED_OUT),
    TOO_LARGE(R.string.TOO_LARGE),
    TOO_LONG(R.string.TOO_LONG),
    TOO_SHORT(R.string.TOO_SHORT),
    TOO_SMALL(R.string.TOO_SMALL),
    TX_NOT_FOUND(R.string.TX_NOT_FOUND),
    WRONG_LOCATION(R.string.WRONG_LOCATION),

    SUCCESS(R.string.SUCCESS);

    val resource get() = _saleStatusResource

    companion object {
        @JvmStatic
        fun getResourceOrOtherError(name: String): Int {
            return values().firstOrNull { it.name == name }?.resource ?: OTHER_ERROR.resource
        }
    }
}

enum class TXType(private val _resource: Int) {
    TRANSFER(R.string.type_transfer),
    SELL(R.string.type_sell),
    SELF_TOPUP(R.string.type_self_topup),
    ADJUST(R.string.type_adjust),
    REVERSE(R.string.type_reverse),
    REVERSE_PARTIALLY(R.string.type_partial_reversal),
    PROMOTION_REWARD(R.string.type_promo_reward),
    ADJUDICATE(R.string.type_adjudicate),
    NON_AIRTIME_DEBIT(R.string.type_non_airtime_debit),
    NON_AIRTIME_REFUND(R.string.type_non_airtime_refund),
    UNKNOWN_TYPE(R.string.unknown);

    val resource get() = _resource

    companion object {
        fun valueOfOrUnknown(name: String): TXType {
            return values().firstOrNull { it.name == name } ?: UNKNOWN_TYPE
        }
    }
}

data class TransactionModel(
    val transactionNo: String,
    val amount: String,
    val costOfGoodsSold: String?,
    val bonus: String,
    val transactionStarted: Long,
    val transactionEnded: Long,
    val sourceMsisdn: String,
    val recipientMsisdn: String,
    val balanceBefore: String,
    val bonusBalanceBefore: String,
    val balanceAfter: String,
    val bonusBalanceAfter: String,
    val status: String,
    val followUpRequired: Boolean,
    val rolledBack: Boolean,
    val messages: List<String>,
    val itemDescription: String?,
    val commissionAmount: String?,
    val type: TXType
)
