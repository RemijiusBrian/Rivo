package dev.ridill.rivo.core.domain.util

import android.icu.util.Currency
import java.util.Locale

object LocaleUtil {
    val defaultLocale: Locale
        get() = Locale.getDefault()

    val defaultCurrency: Currency
        get() = Currency.getInstance(defaultLocale)

    val availableLocales: List<Locale>
        get() = Locale.getAvailableLocales()
            .toList()

    val currencyList: List<Currency>
        get() = availableLocales.mapNotNull {
            tryOrNull { Currency.getInstance(it) }
        }
            .distinctBy { it.currencyCode }

    fun currencyForCode(code: String): Currency = Currency.getInstance(code)
}