package com.ordra.core.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency

/**
 * Decimal-safe money parsing/formatting. Persisted amounts are integer minor
 * units — Float/Double are never used.
 */
object MoneyFormat {

    /** Parses user input ("1,249.50", "649") into minor units. Null when invalid. */
    fun parseToMinor(input: String): Long? {
        val cleaned = input.trim()
            .replace(",", "")
            .replace(Regex("[^\\d.\\-]"), "")
        if (cleaned.isEmpty() || cleaned == "-") return null
        return try {
            BigDecimal(cleaned)
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
        } catch (e: ArithmeticException) {
            null
        } catch (e: NumberFormatException) {
            null
        }
    }

    /** Formats minor units for display, e.g. 64900/PHP -> "PHP 649.00". */
    fun formatMinor(amountMinor: Long?, currencyCode: String): String {
        if (amountMinor == null) return ""
        return try {
            val format = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance(currencyCode)
            format.format(BigDecimal(amountMinor).movePointLeft(2))
        } catch (e: Exception) {
            val major = BigDecimal(amountMinor).movePointLeft(2).setScale(2, RoundingMode.HALF_UP)
            "$currencyCode $major"
        }
    }
}
