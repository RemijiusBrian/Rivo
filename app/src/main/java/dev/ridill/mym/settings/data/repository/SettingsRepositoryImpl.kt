package dev.ridill.mym.settings.data.repository

import android.icu.util.Currency
import dev.ridill.mym.core.domain.util.CurrencyUtil
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.settings.data.local.ConfigDao
import dev.ridill.mym.settings.data.local.ConfigKeys
import dev.ridill.mym.settings.data.local.entity.ConfigEntity
import dev.ridill.mym.settings.domain.modal.BackupInterval
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
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
            code?.let { CurrencyUtil.currencyForCode(it) }
                ?: CurrencyUtil.default
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

    override suspend fun getCurrentBackupInterval(): BackupInterval = withContext(Dispatchers.IO) {
        BackupInterval.valueOf(
            dao.getBackupInterval() ?: BackupInterval.MANUAL.name
        )
    }

    override suspend fun updateBackupInterval(interval: BackupInterval) {
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.insert(entity)
        }
    }
}