package dev.ridill.rivo.settings.domain.repositoty

import android.icu.util.Currency
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    suspend fun updateCurrentBudget(value: Long)
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun updateCurrencyCode(code: String)
    suspend fun getCurrentBackupInterval(): BackupInterval
    suspend fun updateBackupInterval(interval: BackupInterval)
}