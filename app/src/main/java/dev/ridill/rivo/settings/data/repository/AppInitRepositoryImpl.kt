package dev.ridill.rivo.settings.data.repository

import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.settings.data.local.CurrencyDao
import dev.ridill.rivo.settings.data.toEntity
import dev.ridill.rivo.settings.domain.repositoty.AppInitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Currency

class AppInitRepositoryImpl(
    private val currencyDao: CurrencyDao
) : AppInitRepository {
    override suspend fun needsInit(): Boolean = withContext(Dispatchers.IO) {
        currencyDao.isTableEmpty()
    }

    override suspend fun initCurrenciesList() {
        val entities = LocaleUtil.availableLocales
            .mapNotNull {
                tryOrNull { Currency.getInstance(it) }
            }
            .distinctBy { it.currencyCode }
            .map(Currency::toEntity)
            .toTypedArray()
        currencyDao.insert(*entities)
    }
}