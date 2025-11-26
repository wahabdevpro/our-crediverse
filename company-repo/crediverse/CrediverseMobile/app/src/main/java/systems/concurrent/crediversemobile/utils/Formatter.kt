package systems.concurrent.crediversemobile.utils

import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import androidx.core.text.HtmlCompat
import com.google.gson.Gson
import systems.concurrent.crediversemobile.models.CurrencyFormatParams
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Format
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round
import kotlin.math.roundToInt

object Formatter {
    private const val currencyCodesJSON =
        "{\"AED\":\"د.إ\",\"AFN\":\"؋\",\"ALL\":\"L\",\"AMD\":\"֏\",\"ANG\":\"ƒ\"," +
                "\"AOA\":\"Kz\",\"ARS\":\"\$\",\"AUD\":\"\$\",\"AWG\":\"ƒ\",\"AZN\":\"₼\"," +
                "\"BAM\":\"KM\",\"BBD\":\"\$\",\"BDT\":\"৳\",\"BGN\":\"лв\",\"BHD\":\".د.ب\"," +
                "\"BIF\":\"FBu\",\"BMD\":\"\$\",\"BND\":\"\$\",\"BOB\":\"\$b\",\"BOV\":\"BOV\"," +
                "\"BRL\":\"R\$\",\"BSD\":\"\$\",\"BTC\":\"₿\",\"BTN\":\"Nu.\",\"BWP\":\"P\"," +
                "\"BYN\":\"Br\",\"BYR\":\"Br\",\"BZD\":\"BZ\$\",\"CAD\":\"\$\",\"CDF\":\"FC\"," +
                "\"CHE\":\"CHE\",\"CHF\":\"CHF\",\"CHW\":\"CHW\",\"CLF\":\"CLF\",\"CLP\":\"\$\"," +
                "\"CNH\":\"¥\",\"CNY\":\"¥\",\"COP\":\"\$\",\"COU\":\"COU\",\"CRC\":\"₡\"," +
                "\"CUC\":\"\$\",\"CUP\":\"₱\",\"CVE\":\"\$\",\"CZK\":\"Kč\",\"DJF\":\"Fdj\"," +
                "\"DKK\":\"kr\",\"DOP\":\"RD\$\",\"DZD\":\"دج\",\"EEK\":\"kr\",\"EGP\":\"£\"," +
                "\"ERN\":\"Nfk\",\"ETB\":\"Br\",\"ETH\":\"Ξ\",\"EUR\":\"€\",\"FJD\":\"\$\"," +
                "\"FKP\":\"£\",\"GBP\":\"£\",\"GEL\":\"₾\",\"GGP\":\"£\",\"GHC\":\"₵\"," +
                "\"GHS\":\"GH₵\",\"GIP\":\"£\",\"GMD\":\"D\",\"GNF\":\"FG\",\"GTQ\":\"Q\"," +
                "\"GYD\":\"\$\",\"HKD\":\"\$\",\"HNL\":\"L\",\"HRK\":\"kn\",\"HTG\":\"G\"," +
                "\"HUF\":\"Ft\",\"IDR\":\"Rp\",\"ILS\":\"₪\",\"IMP\":\"£\",\"INR\":\"₹\"," +
                "\"IQD\":\"ع.د\",\"IRR\":\"﷼\",\"ISK\":\"kr\",\"JEP\":\"£\",\"JMD\":\"J\$\"," +
                "\"JOD\":\"JD\",\"JPY\":\"¥\",\"KES\":\"KSh\",\"KGS\":\"лв\",\"KHR\":\"៛\"," +
                "\"KMF\":\"CF\",\"KPW\":\"₩\",\"KRW\":\"₩\",\"KWD\":\"KD\",\"KYD\":\"\$\"," +
                "\"KZT\":\"₸\",\"LAK\":\"₭\",\"LBP\":\"£\",\"LKR\":\"₨\",\"LRD\":\"\$\"," +
                "\"LSL\":\"M\",\"LTC\":\"Ł\",\"LTL\":\"Lt\",\"LVL\":\"Ls\",\"LYD\":\"LD\"," +
                "\"MAD\":\"MAD\",\"MDL\":\"lei\",\"MGA\":\"Ar\",\"MKD\":\"ден\",\"MMK\":\"K\"," +
                "\"MNT\":\"₮\",\"MOP\":\"MOP\$\",\"MRO\":\"UM\",\"MRU\":\"UM\",\"MUR\":\"₨\"," +
                "\"MVR\":\"Rf\",\"MWK\":\"MK\",\"MXN\":\"\$\",\"MXV\":\"MXV\",\"MYR\":\"RM\"," +
                "\"MZN\":\"MT\",\"NAD\":\"\$\",\"NGN\":\"₦\",\"NIO\":\"C\$\",\"NOK\":\"kr\"," +
                "\"NPR\":\"₨\",\"NZD\":\"\$\",\"OMR\":\"﷼\",\"PAB\":\"B/.\",\"PEN\":\"S/.\"," +
                "\"PGK\":\"K\",\"PHP\":\"₱\",\"PKR\":\"₨\",\"PLN\":\"zł\",\"PYG\":\"Gs\"," +
                "\"QAR\":\"﷼\",\"RMB\":\"￥\",\"RON\":\"lei\",\"RSD\":\"Дин.\",\"RUB\":\"₽\"," +
                "\"RWF\":\"R₣\",\"SAR\":\"﷼\",\"SBD\":\"\$\",\"SCR\":\"₨\",\"SDG\":\"ج.س.\"," +
                "\"SEK\":\"kr\",\"SGD\":\"S\$\",\"SHP\":\"£\",\"SLL\":\"Le\",\"SOS\":\"S\"," +
                "\"SRD\":\"\$\",\"SSP\":\"£\",\"STD\":\"Db\",\"STN\":\"Db\",\"SVC\":\"\$\"," +
                "\"SYP\":\"£\",\"SZL\":\"E\",\"THB\":\"฿\",\"TJS\":\"SM\",\"TMT\":\"T\"," +
                "\"TND\":\"د.ت\",\"TOP\":\"T\$\",\"TRL\":\"₤\",\"TRY\":\"₺\",\"TTD\":\"TT\$\"," +
                "\"TVD\":\"\$\",\"TWD\":\"NT\$\",\"TZS\":\"TSh\",\"UAH\":\"₴\",\"UGX\":\"USh\"," +
                "\"USD\":\"\$\",\"UYI\":\"UYI\",\"UYU\":\"\$U\",\"UYW\":\"UYW\",\"UZS\":\"лв\"," +
                "\"VEF\":\"Bs\",\"VES\":\"Bs.S\",\"VND\":\"₫\",\"VUV\":\"VT\",\"WST\":\"WS\$\"," +
                "\"XAF\":\"FCFA\",\"XBT\":\"Ƀ\",\"XCD\":\"\$\",\"XOF\":\"CFA\",\"XPF\":\"₣\"," +
                "\"XSU\":\"Sucre\",\"XUA\":\"XUA\",\"YER\":\"﷼\",\"ZAR\":\"R\",\"ZMW\":\"ZK\"," +
                "\"ZWD\":\"Z\$\",\"ZWL\":\"\$\"}"

    private var isUTC = false

    private var currencyFormatParams = CurrencyFormatParams(
        AppFlag.Currency.localeCode, AppFlag.Currency.decimalSeparator,
        AppFlag.Currency.groupSeparator, AppFlag.Currency.symbol, AppFlag.Currency.pattern
    )

    fun setUTC() {
        isUTC = true
    }

    fun getDatestampFromEpoch(epoch: Long): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getDefault()

        val date = Date(epoch * 1000L)
        return dateFormat.format(date)
    }

    fun dateStringFromEpoch(epoch: Long, datePattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val simpleDateFormat = SimpleDateFormat(datePattern, Locale.getDefault())

        if (isUTC) simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date = Date(epoch * 1000)
        return simpleDateFormat.format(date)
    }

    fun formatWithSmallCurrencySuffix(
        text: CharSequence, nbspValue: String = "&thinsp;&nbsp;", sizeDifference: Float = 0.7f
    ): SpannableStringBuilder {
        return combine(
            text, fromHtml(nbspValue),
            adjustSizeRelative("(${AppFlag.Currency.symbol})", sizeDifference)
        )
    }

    fun formatCustomCurrency(value: Double, params: CurrencyFormatParams? = null): String {
        val currencyFormatParams = params ?: currencyFormatParams

        val locale = Locale.forLanguageTag(currencyFormatParams.currencyLocaleCode)
        val customFormatSymbols = DecimalFormatSymbols(locale)
        customFormatSymbols.currencySymbol = currencyFormatParams.currencySymbol
        customFormatSymbols.decimalSeparator = currencyFormatParams.decimalSeparator
        customFormatSymbols.groupingSeparator = currencyFormatParams.groupSeparator

        val customFormat = DecimalFormat(currencyFormatParams.formatPattern, customFormatSymbols)
            .apply {
                this.maximumFractionDigits = currencyFormatParams.decimalPlaces
                this.minimumFractionDigits = currencyFormatParams.decimalPlaces
            }
        return customFormat.format(value)
    }

    fun italic(text: CharSequence): SpannableString {
        val spanText = SpannableString(text)
        spanText.setSpan(StyleSpan(Typeface.ITALIC), 0, text.length, 0)
        return spanText
    }

    fun fromHtml(text: String): CharSequence {
        return HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    fun color(text: CharSequence, color: Int): SpannableString {
        val spanText = SpannableString(text)
        spanText.setSpan(StyleSpan(color), 0, text.length, 0)
        return spanText
    }

    fun normal(text: CharSequence): SpannableString = SpannableString(text)

    fun capitalize(text: CharSequence): SpannableString {
        if (text.isEmpty()) return SpannableString(text)

        val firstChar = text.first().uppercaseChar() // Capitalize the first character
        val remainingChars = text.drop(1) // Get the remaining characters

        return SpannableString(buildString {
            append(firstChar)
            append(remainingChars)
        })
    }

    fun bold(text: CharSequence): SpannableString {
        val spanText = SpannableString(text)
        spanText.setSpan(StyleSpan(Typeface.BOLD), 0, text.length, 0)
        spanText.setSpan(RelativeSizeSpan(1.2F), 0, text.length, 0)
        return spanText
    }

    fun adjustSizeRelative(text: CharSequence, size: Float): SpannableString {
        val spanText = SpannableString(text)
        spanText.setSpan(RelativeSizeSpan(size), 0, text.length, 0)
        return spanText
    }

    fun combine(vararg textArray: CharSequence): SpannableStringBuilder {
        val combinedText = SpannableStringBuilder()
        textArray.forEach { combinedText.append(it) }
        return combinedText
    }

    fun String.stripDecimalsOrNull(): String? {
        return this.toDoubleOrNull()?.roundToInt()?.toString()
    }

    fun Int.toStringWithPaddedDecimalPlaces(
        padCount: Int,
        rounding: RoundingMode = RoundingMode.HALF_DOWN
    ): String {
        return this.toDouble().toStringWithPaddedDecimalPlaces(padCount, rounding)
    }

    fun Long.toStringWithPaddedDecimalPlaces(
        padCount: Int,
        rounding: RoundingMode = RoundingMode.HALF_DOWN
    ): String {
        return this.toDouble().toStringWithPaddedDecimalPlaces(padCount, rounding)
    }

    fun Float.toStringWithPaddedDecimalPlaces(
        padCount: Int,
        rounding: RoundingMode = RoundingMode.HALF_DOWN
    ): String {
        return this.toDouble().toStringWithPaddedDecimalPlaces(padCount, rounding)
    }

    fun Double.toStringWithPaddedDecimalPlaces(
        padCount: Int,
        rounding: RoundingMode = RoundingMode.HALF_DOWN
    ): String {
        return this.toBigDecimal().setScale(padCount, rounding).toString()
    }

    fun String.makeEmptyNull(): String? {
        return if (this.isEmpty()) null else this
    }

    fun String.makeZeroEmpty(): String {
        return if (this == "0") "" else this
    }
}