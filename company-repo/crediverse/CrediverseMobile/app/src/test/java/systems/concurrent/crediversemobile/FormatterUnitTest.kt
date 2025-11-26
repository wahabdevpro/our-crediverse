package systems.concurrent.crediversemobile

import org.junit.Test

import org.junit.Assert.*
import systems.concurrent.crediversemobile.utils.Formatter

class FormatterUnitTest {
    @Test
    fun currencyFormatsCorrectly() {
        val formattedCurrency = Formatter.formatCustomCurrency(12.5)
        assertEquals("12,50 Fcfa", formattedCurrency)
    }

    @Test
    fun dateFormatsCorrectly() {
        Formatter.setUTC()
        var formattedDate = Formatter.dateStringFromEpoch(1675414203L, "yyyy-MM-dd")
        // Uses a NON breaking space in the output
        assertEquals("2023-02-03", formattedDate)

        formattedDate = Formatter.dateStringFromEpoch(1575114201L)
        assertEquals("2019-11-30 11:43:21", formattedDate)
    }
}