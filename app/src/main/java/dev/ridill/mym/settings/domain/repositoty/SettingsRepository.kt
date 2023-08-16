package dev.ridill.mym.settings.domain.repositoty

import dev.ridill.mym.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    suspend fun updateCurrentBudget(value: Long)
    suspend fun getCurrentBackupInterval(): BackupInterval
    suspend fun updateBackupInterval(interval: BackupInterval)
}