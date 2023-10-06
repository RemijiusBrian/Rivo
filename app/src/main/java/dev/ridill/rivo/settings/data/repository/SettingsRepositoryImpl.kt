package dev.ridill.rivo.settings.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dao: ConfigDao
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = dao.getBudgetAmount()
        .map { it.orZero() }
        .distinctUntilChanged()

    override suspend fun updateCurrentBudget(value: Long) {
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BUDGET_AMOUNT,
                configValue = value.toString()
            )
            dao.insert(entity)
        }
    }

    override fun getCurrencyPreference(): Flow<Currency> = dao.getCurrencyCode()
        .map { code ->
            code?.let { LocaleUtil.currencyForCode(it) }
                ?: LocaleUtil.defaultCurrency
        }

    override suspend fun updateCurrencyCode(code: String) {
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.CURRENCY_CODE,
                configValue = code
            )
            dao.insert(entity)
        }
    }
}