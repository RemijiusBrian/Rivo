package dev.ridill.mym.core.ui.util

import dev.ridill.mym.core.domain.util.CurrencyUtil
import dev.ridill.mym.core.domain.util.tryOrNull
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object TextFormat {
    fun currency(
        amount: Double,
        locale: Locale = Locale.getDefault(),
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        currency: Currency = CurrencyUtil.default
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    fun currency(
        amount: Long,
        locale: Locale = Locale.getDefault(),
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        currency: Currency = CurrencyUtil.default
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    fun percent(
        value: Float,
        locale: Locale = Locale.getDefault()
    ): String = NumberFormat.getPercentInstance(locale)
        .format(value)

    fun number(
        value: Double,
        locale: Locale = Locale.getDefault(),
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = NumberFormat.getNumberInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun parseNumber(
        value: String,
        locale: Locale = Locale.getDefault()
    ): Double? = tryOrNull {
        NumberFormat.getNumberInstance(locale)
            .parse(value)
            ?.toDouble()
    }
}

private const val DEFAULT_MAX_FRACTION_DIGITS = 2
private const val DEFAULT_MIN_FRACTION_DIGITS = 0