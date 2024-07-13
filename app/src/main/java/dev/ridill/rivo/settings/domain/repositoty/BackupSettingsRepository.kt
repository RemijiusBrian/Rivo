package dev.ridill.rivo.settings.domain.repositoty

import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface BackupSettingsRepository {
    fun getBackupAccount(): StateFlow<GoogleSignInAccount?>
    fun getLastBackupTime(): Flow<LocalDateTime?>
    fun refreshBackupAccount()
    fun getImmediateBackupWorkInfo(): Flow<WorkInfo?>
    fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?>
    fun getIntervalFromInfo(workInfo: WorkInfo): BackupInterval?
    suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval)
    fun runBackupJob(interval: BackupInterval)
    fun runImmediateBackupJob()
    suspend fun restoreBackupJob()
    fun getFatalBackupError(): Flow<FatalBackupError?>
    fun isEncryptionPasswordAvailable(): Flow<Boolean>
    suspend fun isCurrentPasswordMatch(currentPasswordInput: String): Boolean
    suspend fun updateEncryptionPassword(password: String)
}