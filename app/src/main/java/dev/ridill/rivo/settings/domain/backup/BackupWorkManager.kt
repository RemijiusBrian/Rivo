package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.gson.Gson
import dev.ridill.rivo.core.domain.util.toUUID
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class BackupWorkManager(
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val WORK_INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val BACKUP_DETAILS_INPUT = "BACKUP_DETAILS_INPUT"
    }

    fun schedulePeriodicWorker(interval: BackupInterval) {
        if (interval == BackupInterval.MANUAL) {
            cancelPeriodicBackupWork()
            return
        }

        val workRequest = PeriodicWorkRequestBuilder<GDriveDataBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildConstraints())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACK_OFF_DELAY, TimeUnit.MINUTES)
            .setId(PERIODIC_G_DRIVE_BACKUP_WORK.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${interval.name}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_G_DRIVE_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(PERIODIC_G_DRIVE_BACKUP_WORK.toUUID())
        .asFlow()

    fun cancelPeriodicBackupWork() {
        workManager.cancelWorkById(PERIODIC_G_DRIVE_BACKUP_WORK.toUUID())
    }

    fun runImmediateBackupWork() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(ONE_TIME_G_DRIVE_BACKUP_WORK.toUUID())
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_G_DRIVE_BACKUP_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(ONE_TIME_G_DRIVE_BACKUP_WORK.toUUID())
        .asFlow()

    fun runImmediateRestoreWork(details: BackupDetails) {
        val jsonData = Gson().toJson(details)
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(ONE_TIME_G_DRIVE_RESTORE_WORK.toUUID())
            .setInputData(
                workDataOf(
                    BACKUP_DETAILS_INPUT to jsonData
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            ONE_TIME_G_DRIVE_RESTORE_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateRestoreWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(ONE_TIME_G_DRIVE_RESTORE_WORK.toUUID())
        .asFlow()

    private fun buildConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build()
}

private const val ONE_TIME_G_DRIVE_BACKUP_WORK = "dev.ridill.mym.ONE_TIME_G_DRIVE_BACKUP_WORK"
private const val PERIODIC_G_DRIVE_BACKUP_WORK = "dev.ridill.mym.PERIODIC_G_DRIVE_BACKUP_WORK"
private const val ONE_TIME_G_DRIVE_RESTORE_WORK = "dev.ridill.mym.ONE_TIME_G_DRIVE_RESTORE_WORK"
private const val BACK_OFF_DELAY = 5L