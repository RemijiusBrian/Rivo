package dev.ridill.mym.settings.data.repository

import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.settings.data.local.ConfigKeys
import dev.ridill.mym.settings.data.local.MiscConfigDao
import dev.ridill.mym.settings.data.local.entity.MiscConfigEntity
import dev.ridill.mym.settings.domain.modal.BackupInterval
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val dao: MiscConfigDao,
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = dao.getBudgetAmount()
        .map { it.toLongOrNull().orZero() }
        .distinctUntilChanged()

    override suspend fun updateCurrentBudget(value: Long) {
        withContext(Dispatchers.IO) {
            val entity = MiscConfigEntity(
                configKey = ConfigKeys.BUDGET_AMOUNT,
                configValue = value.toString()
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
            val entity = MiscConfigEntity(
                configKey = ConfigKeys.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.insert(entity)
        }
    }
}