package dev.ridill.rivo.settings.data.repository

import androidx.work.WorkInfo
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import dev.ridill.rivo.settings.domain.repositoty.FatalBackupError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class BackupSettingsRepositoryImpl(
    private val dao: ConfigDao,
    private val preferencesManager: PreferencesManager,
    private val backupWorkManager: BackupWorkManager,
    private val cryptoManager: CryptoManager
) : BackupSettingsRepository {

    override fun getLastBackupTime(): Flow<LocalDateTime?> = preferencesManager.preferences
        .map { it.lastBackupDateTime }
        .distinctUntilChanged()

    override fun getImmediateBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getImmediateBackupWorkInfoFlow()

    override fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getPeriodicBackupWorkInfoFlow()

    override fun getIntervalFromInfo(workInfo: WorkInfo): BackupInterval? =
        backupWorkManager.getBackupIntervalFromWorkInfo(workInfo)

    override suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval) =
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.upsert(entity)

            if (interval == BackupInterval.MANUAL) {
                backupWorkManager.cancelPeriodicBackupWork()
            } else {
                backupWorkManager.schedulePeriodicBackupWork(interval)
            }
        }

    override fun runBackupJob(interval: BackupInterval) {
        if (interval == BackupInterval.MANUAL) {
            backupWorkManager.runImmediateBackupWork()
        } else {
            backupWorkManager.schedulePeriodicBackupWork(interval)
        }
    }

    override fun runImmediateBackupJob() {
        backupWorkManager.runImmediateBackupWork()
    }

    override suspend fun restoreBackupJob() {
        val backupInterval = BackupInterval.valueOf(
            dao.getBackupInterval() ?: BackupInterval.MANUAL.name
        )
        backupWorkManager.schedulePeriodicBackupWork(backupInterval)
    }

    override suspend fun isCurrentPasswordMatch(currentPasswordInput: String): Boolean =
        preferencesManager.preferences.first().encryptionPasswordHash?.let {
            cryptoManager.areDigestsEqual(
                hash1 = cryptoManager.hash(currentPasswordInput),
                hash2 = it
            )
        } ?: false


    override suspend fun updateEncryptionPassword(password: String): Unit =
        withContext(Dispatchers.IO) {
            val passwordHash = cryptoManager.hash(password)
            preferencesManager.updateEncryptionPasswordHash(passwordHash)
        }

    override fun getFatalBackupError(): Flow<FatalBackupError?> = preferencesManager
        .preferences
        .map { it.fatalBackupError }
        .distinctUntilChanged()

    override fun isEncryptionPasswordAvailable(): Flow<Boolean> =
        preferencesManager.preferences
            .map { it.encryptionPasswordHash }
            .map { !it.isNullOrEmpty() }
            .distinctUntilChanged()
}

