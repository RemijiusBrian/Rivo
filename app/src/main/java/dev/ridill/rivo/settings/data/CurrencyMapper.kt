package dev.ridill.rivo.settings.data

import dev.ridill.rivo.settings.data.local.entity.CurrencyListEntity
import java.util.Currency

fun Currency.toEntity(): CurrencyListEntity = CurrencyListEntity(
    currencyCode = currencyCode,
    displayName = displayName
)