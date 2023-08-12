package dev.ridill.mym.settings.domain.backup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dev.ridill.mym.settings.domain.modal.BackupInterval
import java.util.concurrent.TimeUnit

class BackupWorkManager(
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun runBackupWorkerOnceNow(): LiveData<WorkInfo> {
        val workRequest = OneTimeWorkRequestBuilder<GDriveBackupWorker>()
            .build()

        workManager.enqueue(workRequest)

        return workManager.getWorkInfoByIdLiveData(workRequest.id)
    }

    fun schedulePeriodicWorker(interval: BackupInterval) {
        if (interval == BackupInterval.NEVER || interval == BackupInterval.MANUAL) return

        val workRequest = PeriodicWorkRequestBuilder<GDriveBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildConstraints())
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    private fun buildConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build()
}

private const val WORK_NAME = "dev.ridill.mym.G_DRIVE_BACKUP_WORK"