package dev.ridill.rivo.core.ui.util

import android.icu.text.CompactDecimalFormat
import android.icu.text.CompactDecimalFormat.CompactStyle
import android.icu.text.NumberFormat
import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.tryOrNull
import java.util.Locale

object TextFormat {
    fun currency(
        amount: Double,
        currency: Currency = LocaleUtil.defaultCurrency,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = currency.defaultFractionDigits,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    fun currency(
        amount: Long,
        currency: Currency = LocaleUtil.defaultCurrency,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = currency.defaultFractionDigits,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS
    ): String = NumberFormat.getCurrencyInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            setCurrency(currency)
        }
        .format(amount)

    fun number(
        value: Double,
        locale: Locale = LocaleUtil.defaultLocale,
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

    fun number(
        value: Long,
        locale: Locale = LocaleUtil.defaultLocale,
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

    fun compactNumber(
        value: Double,
        locale: Locale = LocaleUtil.defaultLocale,
        currency: Currency = LocaleUtil.defaultCurrency,
        compactStyle: CompactStyle = CompactStyle.SHORT,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = CompactDecimalFormat.getInstance(locale, compactStyle)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.currency = currency
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun percent(
        value: Float,
        locale: Locale = LocaleUtil.defaultLocale,
        maxFractionDigits: Int = DEFAULT_MAX_FRACTION_DIGITS,
        minFractionDigits: Int = DEFAULT_MIN_FRACTION_DIGITS,
        isGroupingUsed: Boolean = true
    ): String = NumberFormat.getPercentInstance(locale)
        .apply {
            maximumFractionDigits = maxFractionDigits
            minimumFractionDigits = minFractionDigits
            this.isGroupingUsed = isGroupingUsed
        }
        .format(value)

    fun parseNumber(
        value: String,
        locale: Locale = LocaleUtil.defaultLocale
    ): Double? = tryOrNull {
        NumberFormat.getNumberInstance(locale)
            .parse(value)
            ?.toDouble()
    }
}

private const val DEFAULT_MAX_FRACTION_DIGITS = 2
private const val DEFAULT_MIN_FRACTION_DIGITS = 0