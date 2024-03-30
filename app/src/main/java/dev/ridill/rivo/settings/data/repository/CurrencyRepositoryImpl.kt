package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.settings.data.local.CurrencyDao
import dev.ridill.rivo.settings.data.local.entity.CurrencyEntity
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.Currency

class CurrencyRepositoryImpl(
    private val dao: CurrencyDao
) : CurrencyRepository {
    override fun getCurrencyForDateOrNext(date: LocalDate): Flow<Currency> = dao
        .getCurrencyCodeForDateOrNext(date)
        .map { currencyCode ->
            currencyCode?.let {
                tryOrNull { LocaleUtil.currencyForCode(it) }
            } ?: LocaleUtil.defaultCurrency
        }.distinctUntilChanged()

    override suspend fun saveCurrency(currency: Currency, date: LocalDate) {
        withContext(Dispatchers.IO) {
            val entity = CurrencyEntity(
                currencyCode = currency.currencyCode,
                date = date.withDayOfMonth(1)
            )
            dao.insert(entity)
        }
    }
}