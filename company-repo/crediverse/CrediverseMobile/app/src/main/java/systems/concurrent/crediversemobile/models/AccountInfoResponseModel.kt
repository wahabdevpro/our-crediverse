package systems.concurrent.crediversemobile.models

import systems.concurrent.crediversemobile.R

enum class AccountState(private val _stateResource: Int) {
    ACTIVE(R.string.active),
    SUSPENDED(R.string.suspended),
    DEACTIVATED(R.string.deactivated),
    PERMANENT(R.string.permanent),
    UNKNOWN(R.string.unknown);

    val resource get() = _stateResource
}

enum class AccountLanguage(private val _langResource: Int) {
    EN(R.string.english),
    FR(R.string.french),
    UN(R.string.unknown);

    val resource get() = _langResource

    companion object {
        @JvmStatic
        fun getResourceOrUnknown(lang: String): Int =
            valueOfOrNull(lang)?.resource ?: UN._langResource

        @JvmStatic
        fun valueOfOrNull(name: String): AccountLanguage? =
            values().firstOrNull { it.name == name.uppercase() }
    }
}

data class AccountInfoResponseModel(
    val accountNumber: String?,
    val msisdn: String,
    var title: String?,
    var firstName: String,
    val initials: String?,
    var surname: String?,
    var language: String?,
    val altPhoneNumber: String?,
    var email: String?,
    val accountState: AccountState,
    val activationDate: Int,
    val countryCode: String,
    val tierName: String,
)
