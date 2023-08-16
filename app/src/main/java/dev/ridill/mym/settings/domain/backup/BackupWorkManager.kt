package dev.ridill.mym.settings.domain.backup

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
import dev.ridill.mym.settings.domain.modal.BackupDetails
import dev.ridill.mym.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import java.util.concurrent.TimeUnit

class BackupWorkManager(
    context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
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
            .setId(getUUIDFromName(PERIODIC_G_DRIVE_BACKUP_WORK))
            .addTag("$INTERVAL_TAG_PREFIX${interval.name}")
            .build()

        workManager.enqueueUniquePeriodicWork(
            PERIODIC_G_DRIVE_BACKUP_WORK,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(getUUIDFromName(PERIODIC_G_DRIVE_BACKUP_WORK))
        .asFlow()

    fun cancelPeriodicBackupWork() {
        workManager.cancelWorkById(getUUIDFromName(PERIODIC_G_DRIVE_BACKUP_WORK))
    }

    fun runImmediateBackupWork() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(getUUIDFromName(IMMEDIATE_G_DRIVE_BACKUP_WORK))
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_G_DRIVE_BACKUP_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(getUUIDFromName(IMMEDIATE_G_DRIVE_BACKUP_WORK))
        .asFlow()

    fun runImmediateRestoreWork(details: BackupDetails) {
        val jsonData = Gson().toJson(details)
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(getUUIDFromName(IMMEDIATE_G_DRIVE_RESTORE_WORK))
            .setInputData(
                workDataOf(
                    BACKUP_DETAILS_INPUT to jsonData
                )
            )
            .build()

        workManager.enqueueUniqueWork(
            IMMEDIATE_G_DRIVE_RESTORE_WORK,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateRestoreWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(getUUIDFromName(IMMEDIATE_G_DRIVE_RESTORE_WORK))
        .asFlow()

    private fun getUUIDFromName(name: String): UUID =
        UUID.nameUUIDFromBytes(name.toByteArray())

    private fun buildConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build()
}

private const val IMMEDIATE_G_DRIVE_BACKUP_WORK = "dev.ridill.mym.IMMEDIATE_G_DRIVE_BACKUP_WORK"
private const val PERIODIC_G_DRIVE_BACKUP_WORK = "dev.ridill.mym.PERIODIC_G_DRIVE_BACKUP_WORK"
private const val IMMEDIATE_G_DRIVE_RESTORE_WORK = "dev.ridill.mym.IMMEDIATE_G_DRIVE_RESTORE_WORK"
private const val BACK_OFF_DELAY = 5L