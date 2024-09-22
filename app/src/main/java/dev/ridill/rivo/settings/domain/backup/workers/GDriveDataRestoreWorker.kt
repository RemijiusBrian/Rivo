package dev.ridill.rivo.settings.domain.backup.workers

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.di.BackupFeature
import dev.ridill.rivo.settings.data.repository.BackupDownloadFailedThrowable
import dev.ridill.rivo.settings.data.repository.InvalidEncryptionPasswordThrowable
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import kotlin.coroutines.cancellation.CancellationException

@HiltWorker
class GDriveDataRestoreWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        try {
            val passwordHash = inputData.getString(BackupWorkManager.KEY_PASSWORD_HASH).orEmpty()
                .ifEmpty { throw InvalidEncryptionPasswordThrowable() }
            val timestamp = inputData.getString(BackupWorkManager.KEY_BACKUP_TIMESTAMP)
                ?.let { DateUtil.parseDateTimeOrNull(it) }
                ?: throw BackupDownloadFailedThrowable()
            logI(GDriveDataRestoreWorker::class.simpleName) { "Starting data restore from cache" }
            repo.performAppDataRestoreFromCache(passwordHash, timestamp)
            logI(GDriveDataRestoreWorker::class.simpleName) { "Backup Restored" }
            repo.tryClearLocalCache()
            logI(GDriveDataRestoreWorker::class.simpleName) { "Cleared cache" }
            Result.success()
        } catch (t: InvalidEncryptionPasswordThrowable) {
            logE(
                t,
                GDriveDataRestoreWorker::class.simpleName
            ) { "InvalidEncryptionPasswordThrowable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_invalid_encryption_password)
                )
            )
        } catch (e: IllegalBlockSizeException) {
            logE(e, GDriveDataRestoreWorker::class.simpleName) { "IllegalBlockSizeException" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_incorrect_encryption_password)
                )
            )
        } catch (e: BadPaddingException) {
            logE(e, GDriveDataRestoreWorker::class.simpleName) { "BadPaddingException" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_incorrect_encryption_password)
                )
            )
        } catch (t: BackupDownloadFailedThrowable) {
            logE(t, GDriveDataRestoreWorker::class.simpleName) { "BackupDownloadFailedThrowable" }
            Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_download_backup_failed)
                )
            )
        } catch (t: Throwable) {
            logE(t, GDriveDataRestoreWorker::class.simpleName) { "Throwable" }
            if (t is CancellationException) throw t
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
                BackupWorkManager.RESTORE_WORKER_NOTIFICATION_ID.hashCode(),
                notificationHelper.buildBaseNotification()
                    .setContentTitle(appContext.getString(R.string.restoring_app_data))
                    .setProgress(100, 0, true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}