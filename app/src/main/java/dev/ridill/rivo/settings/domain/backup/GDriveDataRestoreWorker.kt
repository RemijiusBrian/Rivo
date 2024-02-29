package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.settings.data.repository.BackupDownloadFailedThrowable
import dev.ridill.rivo.settings.data.repository.InvalidEncryptionPasswordThrowable
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.notification.BackupNotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class GDriveDataRestoreWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    private val notificationHelper: BackupNotificationHelper,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        try {
            val backupDetails = tryOrNull {
                Gson().fromJson(
                    inputData.getString(BackupWorkManager.KEY_DETAILS_INPUT).orEmpty(),
                    BackupDetails::class.java
                )
            } ?: throw Throwable()

            val passwordHash = inputData.getString(BackupWorkManager.KEY_PASSWORD_HASH).orEmpty()
                .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
            repo.performAppDataRestore(backupDetails, passwordHash)
            preferencesManager.updateNeedsConfigRestore(true)
            logI { "Backup Restored" }
            Result.success()
        } catch (t: InvalidEncryptionPasswordThrowable) {
            logE(t) { "InvalidEncryptionPasswordThrowable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_invalid_encryption_password)
                )
            )
        } catch (t: BackupDownloadFailedThrowable) {
            logE(t) { "BackupDownloadFailedThrowable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_download_backup_failed)
                )
            )
        } catch (t: Throwable) {
            logE(t) { "Throwable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_app_data_restore_failed)
                )
            )
        }
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                notificationHelper.buildForegroundNotification(R.string.restoring_app_data).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}