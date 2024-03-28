package dev.ridill.rivo.settings.data.repository

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.crypto.CryptoManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.settings.data.local.ConfigDao
import dev.ridill.rivo.settings.data.local.ConfigKeys
import dev.ridill.rivo.settings.data.local.entity.ConfigEntity
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import dev.ridill.rivo.settings.domain.repositoty.BackupSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class BackupSettingsRepositoryImpl(
    private val dao: ConfigDao,
    private val signInService: GoogleSignInService,
    private val preferencesManager: PreferencesManager,
    private val backupWorkManager: BackupWorkManager,
    private val cryptoManager: CryptoManager
) : BackupSettingsRepository {
    private val _backupAccount = MutableStateFlow<GoogleSignInAccount?>(null)
    private val _backupInterval = MutableStateFlow(BackupInterval.MANUAL)

    override fun getBackupAccount(): StateFlow<GoogleSignInAccount?> =
        _backupAccount.asStateFlow()

    override fun getLastBackupTime(): Flow<LocalDateTime?> = preferencesManager.preferences
        .map { it.lastBackupDateTime }
        .distinctUntilChanged()

    override fun refreshBackupAccount() {
        _backupAccount.update {
            signInService.getSignedInAccount()
        }
    }

    override suspend fun getSignInIntent(): Intent =
        signInService.getSignInIntent()

    override suspend fun signInUser(result: ActivityResult): Resource<GoogleSignInAccount> =
        withContext(Dispatchers.IO) {
            if (result.resultCode != Activity.RESULT_OK) {
                backupWorkManager.cancelAllWorks()
                return@withContext Resource.Error(
                    UiText.StringResource(
                        R.string.error_sign_in_failed,
                        true
                    )
                )
            }

            val resource = signInService.getAccountFromSignInResult(result)
            _backupAccount.update { resource.data }
            resource
        }

    override fun getImmediateBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getImmediateBackupWorkInfoFlow()

    override fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getPeriodicBackupWorkInfoFlow()
            .onEach { info ->
                info?.let { backupWorkManager.getBackupIntervalFromWorkInfo(it) }
                    ?.let { interval ->
                        _backupInterval.update { interval }
                    }
            }

    override suspend fun updateBackupIntervalAndScheduleJob(interval: BackupInterval) =
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.insert(entity)

            if (signInService.getSignedInAccount() == null) {
                backupWorkManager.cancelAllWorks()
                return@withContext
            }
            if (interval == BackupInterval.MANUAL) {
                backupWorkManager.cancelPeriodicBackupWork()
                backupWorkManager.runImmediateBackupWork()
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

    override fun getBackupInterval(): StateFlow<BackupInterval> = _backupInterval.asStateFlow()
}

