package dev.ridill.mym.settings.domain.backup

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.mym.R
import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.model.Resource
import dev.ridill.mym.core.ui.util.UiText
import dev.ridill.mym.settings.domain.notification.BackupNotificationHelper
import dev.ridill.mym.settings.domain.repositoty.BackupRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class GDriveDataBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val repo: BackupRepository,
    private val notificationHelper: BackupNotificationHelper,
    private val preferencesManager: PreferencesManager
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForegroundService()
        when (val resource = repo.performAppDataBackup()) {
            is Resource.Error -> {
                val message = resource.message?.asString(appContext)
                preferencesManager.updateBackupWorkerMessage(message)
                Result.failure(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to message
                    )
                )
            }

            is Resource.Success -> {
                val message = UiText.StringResource(R.string.backup_complete).asString(appContext)
                preferencesManager.updateBackupWorkerMessage(message)
                Result.success(
                    workDataOf(
                        BackupWorkManager.KEY_MESSAGE to message
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