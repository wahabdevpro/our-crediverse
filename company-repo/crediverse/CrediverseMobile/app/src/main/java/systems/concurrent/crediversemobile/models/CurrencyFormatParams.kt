package systems.concurrent.crediversemobile.models

import systems.concurrent.crediversemobile.utils.AppFlag

data class CurrencyFormatParams(
    val currencyLocaleCode: String = AppFlag.Currency.localeCode,
    val decimalSeparator: Char = AppFlag.Currency.decimalSeparator,
    val groupSeparator: Char = AppFlag.Currency.groupSeparator,
    val currencySymbol: String = AppFlag.Currency.symbol,
    private var _formatPattern: String = AppFlag.Currency.pattern,
    private var _decimalPlaces: Int = 2
) {
    val formatPattern get() = _formatPattern
    val decimalPlaces get() = _decimalPlaces

    fun withNoDecimalPlaces(): CurrencyFormatParams {
        _decimalPlaces = 0
        return this
    }

    fun withTwoDecimalPlaces(): CurrencyFormatParams {
        _decimalPlaces = 2
        return this
    }

    fun useWithoutCurrencyPattern(): CurrencyFormatParams {
        _formatPattern = AppFlag.Currency.patternWithoutSymbol
        return this
    }
}
