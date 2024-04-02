package dev.ridill.rivo.settings.domain.repositoty

import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import java.time.LocalDateTime

interface BackupRepository {
    suspend fun checkForBackup(): Resource<BackupDetails>
    suspend fun performAppDataBackup()
    suspend fun downloadAndCacheBackupData(fileId: String, timestamp: LocalDateTime)
    suspend fun performAppDataRestoreFromCache(passwordHash: String, timestamp: LocalDateTime)
    suspend fun tryClearLocalCache()
    suspend fun setBackupError(error: FatalBackupError?)
    suspend fun restoreAppConfig()
}

enum class FatalBackupError { PASSWORD_CORRUPTED, GOOGLE_AUTH_FAILURE, STORAGE_QUOTA_EXCEEDED }