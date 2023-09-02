package dev.ridill.mym.core.domain.util

import java.util.Currency
import java.util.Locale

object CurrencyUtil {
    val default: Currency get() = Currency.getInstance(Locale.getDefault())
    val currencyList: List<Currency>
        get() = Currency.getAvailableCurrencies()
            .toList()

    fun currencyForCode(code: String): Currency = Currency.getInstance(code)
}