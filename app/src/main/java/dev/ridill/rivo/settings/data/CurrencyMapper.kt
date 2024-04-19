package dev.ridill.rivo.settings.data

import dev.ridill.rivo.settings.data.local.entity.CurrencyEntity
import java.util.Currency

fun Currency.toEntity(): CurrencyEntity = CurrencyEntity(
    currencyCode = currencyCode,
    displayName = displayName
)