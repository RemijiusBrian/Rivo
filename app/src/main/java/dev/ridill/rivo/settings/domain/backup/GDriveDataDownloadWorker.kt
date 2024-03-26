package dev.ridill.rivo.settings.domain.backup

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
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class GDriveDataDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        val timestamp = inputData.getString(BackupWorkManager.KEY_BACKUP_TIMESTAMP)
            ?.let { DateUtil.parseDateTime(it) }
            ?: return@withContext Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to appContext.getString(R.string.error_download_backup_failed)
                )
            )
        try {
            val backupFileId = inputData.getString(BackupWorkManager.KEY_BACKUP_FILE_ID)
                ?: throw BackupDownloadFailedThrowable()
            repo.downloadAndCacheBackupData(backupFileId, timestamp)
            logI { "Backup data downloaded and cached" }
            Result.success(
                workDataOf(
                    BackupWorkManager.KEY_BACKUP_TIMESTAMP to timestamp.toString()
                )
            )
        } catch (t: RestoreCacheAlreadyExistsThrowable) {
            logE(t) { "RestoreCacheAlreadyExistsThrowable" }
            logI { "Backup data already exists, so moving forward with restore" }
            Result.success(
                workDataOf(
                    BackupWorkManager.KEY_BACKUP_TIMESTAMP to timestamp.toString()
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
                BackupWorkManager.RESTORE_WORKER_NOTIFICATION_ID.hashCode(),
                notificationHelper.buildBaseNotification()
                    .setContentTitle(appContext.getString(R.string.downloading_app_data))
                    .setProgress(100, 0, true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}