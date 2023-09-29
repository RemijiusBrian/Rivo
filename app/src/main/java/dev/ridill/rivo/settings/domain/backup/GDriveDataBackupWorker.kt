package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.settings.domain.notification.BackupNotificationHelper
import dev.ridill.rivo.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class GDriveDataBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    private val notificationHelper: BackupNotificationHelper
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        when (val resource = repo.performAppDataBackup()) {
            is Resource.Error -> {
                Result.failure(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to resource.message?.asString(appContext)
                    )
                )
            }

            is Resource.Success -> {
                Result.success(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to resource.message?.asString(appContext)
                    )
                )
            }
        }
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                notificationHelper.getForegroundNotification(R.string.backing_up_app_data).build()
            )
        )
    }
}