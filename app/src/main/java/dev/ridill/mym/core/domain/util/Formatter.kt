package dev.ridill.mym.core.domain.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object Formatter {
    fun currency(
        amount: Double,
        locale: Locale = Locale.getDefault(),
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
        }
        .format(amount)

    fun currency(
        amount: Long,
        locale: Locale = Locale.getDefault(),
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat
        .getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
        }
        .format(amount)

    fun currencySymbol(
        locale: Locale = Locale.getDefault()
    ): String = Currency.getInstance(locale).symbol
}

private const val DEFAULT_MAX_FRACTION_DIGITS = 2
private const val DEFAULT_MIN_FRACTION_DIGITS = 0