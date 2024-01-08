package dev.ridill.rivo.settings.domain.backup

import android.content.Context
import androidx.lifecycle.asFlow
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
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    private val commonBackupTag: String
        get() = "${context.packageName}.BACKUP"

    private val oneTimeBackupWorkName: String
        get() = "${context.packageName}.ONE_TIME_G_DRIVE_BACKUP_WORK"

    private val periodicBackupWorkName: String
        get() = "${context.packageName}.PERIODIC_G_DRIVE_BACKUP_WORK"

    private val oneTimeRestoreWorkName: String
        get() = "${context.packageName}.ONE_TIME_G_DRIVE_RESTORE_WORK"

    companion object {
        const val WORK_INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val BACKUP_DETAILS_INPUT = "BACKUP_DETAILS_INPUT"
    }

    fun schedulePeriodicBackupWork(interval: BackupInterval) {
        if (interval == BackupInterval.MANUAL) {
            cancelPeriodicBackupWork()
            return
        }

        val workRequest = PeriodicWorkRequestBuilder<GDriveDataBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildConstraints())
            .setId(periodicBackupWorkName.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${interval.name}")
            .addTag(commonBackupTag)
            .build()

        workManager.enqueueUniquePeriodicWork(
            periodicBackupWorkName,
            ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
            workRequest
        )
    }

    fun getPeriodicBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(periodicBackupWorkName.toUUID())
        .asFlow()

    fun cancelPeriodicBackupWork() {
        workManager.cancelWorkById(periodicBackupWorkName.toUUID())
    }

    fun runImmediateBackupWork() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(oneTimeBackupWorkName.toUUID())
            .addTag(commonBackupTag)
            .build()

        workManager.enqueueUniqueWork(
            oneTimeBackupWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(oneTimeBackupWorkName.toUUID())
        .asFlow()

    fun runImmediateRestoreWork(details: BackupDetails) {
        val jsonData = Gson().toJson(details)
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(oneTimeRestoreWorkName.toUUID())
            .setInputData(
                workDataOf(
                    BACKUP_DETAILS_INPUT to jsonData
                )
            )
            .addTag(commonBackupTag)
            .build()

        workManager.enqueueUniqueWork(
            oneTimeRestoreWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateRestoreWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdLiveData(oneTimeRestoreWorkName.toUUID())
        .asFlow()

    fun cancelAllWorks() {
        workManager.cancelAllWorkByTag(commonBackupTag)
    }

    private fun buildConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresDeviceIdle(false)
        .build()
}