package dev.ridill.rivo.core.domain.util

import android.icu.util.Currency
import java.util.Locale

object CurrencyUtil {
    val default: Currency get() = Currency.getInstance(Locale.getDefault())
    val currencyList: List<Currency>
        get() = Locale.getAvailableLocales().mapNotNull {
            tryOrNull { Currency.getInstance(it) }
        }
            .distinctBy { it.currencyCode }

    fun currencyForCode(code: String): Currency = Currency.getInstance(code)
}