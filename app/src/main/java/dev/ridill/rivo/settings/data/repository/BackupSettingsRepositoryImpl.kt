package dev.ridill.rivo.settings.data.repository

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.work.WorkInfo
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.service.GoogleSignInService
import dev.ridill.rivo.core.domain.util.toInt
import dev.ridill.rivo.core.domain.util.tryOrNull
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class BackupSettingsRepositoryImpl(
    private val dao: ConfigDao,
    private val signInService: GoogleSignInService,
    private val preferencesManager: PreferencesManager,
    private val backupWorkManager: BackupWorkManager
) : BackupSettingsRepository {
    private val _backupAccount = MutableStateFlow<GoogleSignInAccount?>(null)

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
            refreshBackupAccount()

            if (result.resultCode != Activity.RESULT_OK) {
                backupWorkManager.cancelAllWorks()
                return@withContext Resource.Error(
                    UiText.StringResource(
                        R.string.error_sign_in_failed,
                        true
                    )
                )
            }

            val account = tryOrNull { signInService.getAccountFromIntent(result.data) }
            if (account == null) {
                backupWorkManager.cancelAllWorks()
                Resource.Error(
                    UiText.StringResource(
                        R.string.error_sign_in_failed,
                        true
                    )
                )
            } else {
                _backupAccount.update { account }
                Resource.Success(account)
            }
        }

    override fun getImmediateBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getImmediateBackupWorkInfoFlow()

    override fun getPeriodicBackupWorkInfo(): Flow<WorkInfo?> =
        backupWorkManager.getPeriodicBackupWorkInfoFlow()

    override suspend fun updateBackupInterval(interval: BackupInterval) =
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BACKUP_INTERVAL,
                configValue = interval.name
            )
            dao.insert(entity)

            if (signInService.getSignedInAccount() == null) {
                backupWorkManager.cancelPeriodicBackupWork()
                return@withContext
            }
            val runInCellular = getBackupUsingCellular().first()

            if (interval == BackupInterval.MANUAL)
                backupWorkManager.cancelPeriodicBackupWork()
            else
                backupWorkManager.schedulePeriodicBackupWork(
                    interval = interval,
                    runInCellular = runInCellular
                )
        }


    override fun runImmediateBackupJob() {
        backupWorkManager.runImmediateBackupWork()
    }

    override fun getBackupUsingCellular(): Flow<Boolean> = dao.getBackupUsingCellular()
        .map { it == true }
        .distinctUntilChanged()

    override suspend fun updateBackupUsingCellular(checked: Boolean, interval: BackupInterval) =
        withContext(Dispatchers.IO) {
            val entity = ConfigEntity(
                configKey = ConfigKeys.BACKUP_USING_CELLULAR,
                configValue = checked.toInt().toString()
            )
            dao.insert(entity)

            backupWorkManager.schedulePeriodicBackupWork(
                interval = interval,
                runInCellular = checked
            )
        }

    override suspend fun getCurrentBackupInterval(): BackupInterval =
        BackupInterval.valueOf(
            dao.getBackupInterval() ?: BackupInterval.MANUAL.name
        )
}