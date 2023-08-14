package dev.ridill.mym.settings.domain.backup

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
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
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .build()

        workManager.enqueue(workRequest)

        return workManager.getWorkInfoByIdLiveData(workRequest.id)
    }

    fun schedulePeriodicWorker(interval: BackupInterval) {
        if (interval == BackupInterval.NEVER || interval == BackupInterval.MANUAL) return

        val workRequest = PeriodicWorkRequestBuilder<GDriveDataBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACK_OFF_DELAY, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun runRestoreWorkerNow() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .build()

        workManager.enqueue(workRequest)
    }

//    fun getRestoreWorkInfoLiveData(): LiveData<WorkInfo> = workManager
//        .getWorkInfoByIdLiveData(UUID.nameUUIDFromBytes(RESTORE_WORK_ID_STRING.toByteArray()))

    private fun buildConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build()
}

private const val RESTORE_WORK_ID_STRING = "1000"
private const val WORK_NAME = "dev.ridill.mym.G_DRIVE_BACKUP_WORK"
private const val BACK_OFF_DELAY = 5L