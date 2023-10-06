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
import dev.ridill.rivo.core.domain.model.Resource
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.core.ui.util.UiText
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
        val backupDetails = tryOrNull {
            Gson().fromJson(
                inputData.getString(BackupWorkManager.BACKUP_DETAILS_INPUT).orEmpty(),
                BackupDetails::class.java
            )
        } ?: return@withContext Result.failure(
            workDataOf(
                BackupWorkManager.KEY_MESSAGE to UiText.StringResource(R.string.error_no_backup_found)
                    .asString(appContext)
            )
        )

        when (val resource = repo.performAppDataRestore(backupDetails)) {
            is Resource.Error -> Result.failure(
                workDataOf(
                    BackupWorkManager.KEY_MESSAGE to resource.message?.asString(appContext)
                )
            )

            is Resource.Success -> {
                preferencesManager.updateNeedsConfigRestore(true)
                Result.success()
            }
        }
    }

    private suspend fun startForegroundService() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                notificationHelper.getForegroundNotification(R.string.restoring_app_data).build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}