package dev.ridill.rivo.settings.domain.repositoty

import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

interface BackupSettingsRepository {
    fun getBackupAccount(): StateFlow<GoogleSignInAccount?>
    fun getLastBackupTime(): Flow<LocalDateTime?>
    fun refreshBackupAccount()
    suspend fun getSignInIntent(): Intent
    suspend fun signInUser(result: ActivityResult): Resource<GoogleSignInAccount>
    fun getImmediateBackupWorkInfo(): Flow<WorkInfo?>
    fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?>
    fun getIntervalFromInfo(workInfo: WorkInfo): BackupInterval?
    suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval)
    fun runBackupJob(interval: BackupInterval)
    fun runImmediateBackupJob()
    suspend fun restoreBackupJob()
    suspend fun isCurrentPasswordMatch(currentPasswordInput: String): Boolean
    suspend fun updateEncryptionPassword(password: String)
}