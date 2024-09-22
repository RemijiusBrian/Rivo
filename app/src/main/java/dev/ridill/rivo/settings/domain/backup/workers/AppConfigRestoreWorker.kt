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
import dev.ridill.rivo.core.domain.util.logE
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.di.BackupFeature
import dev.ridill.rivo.settings.domain.backup.BackupWorkManager
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AppConfigRestoreWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    @BackupFeature private val notificationHelper: NotificationHelper<String>,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        try {
            logI(AppConfigRestoreWorker::class.simpleName) { "Starting config restore" }
            repo.restoreAppConfig()
            logI(AppConfigRestoreWorker::class.simpleName) { "Config restore complete" }
            Result.success()
        } catch (t: Throwable) {
            logE(t, AppConfigRestoreWorker::class.simpleName) { "Throwable" }
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
                    .setContentTitle(appContext.getString(R.string.restoring_app_config))
                    .setProgress(100, 0, true)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    .build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}