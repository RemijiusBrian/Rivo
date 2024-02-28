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
import androidx.work.WorkRequest
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

    private val backupRestoreWorkerTag: String
        get() = "${context.packageName}.DATA_BACKUP_RESTORE_WORKER"

    private val oneTimeBackupWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_BACKUP_WORK"

    private val periodicBackupWorkName: String
        get() = "${context.packageName}.PERIODIC_DATA_BACKUP_WORK"

    private val oneTimeRestoreWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_RESTORE_WORK"

    companion object {
        const val WORK_INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val KEY_DETAILS_INPUT = "KEY_DETAILS_INPUT"
        const val KEY_PASSWORD_HASH = "KEY_PASSWORD_HASH"
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
            .addTag(backupRestoreWorkerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.DEFAULT_BACKOFF_DELAY_MILLIS,
                TimeUnit.MILLISECONDS
            )
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
            .addTag(backupRestoreWorkerTag)
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

    fun runImmediateRestoreWork(details: BackupDetails, passwordHash: String) {
        val jsonData = Gson().toJson(details)
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(oneTimeRestoreWorkName.toUUID())
            .setInputData(
                workDataOf(
                    KEY_DETAILS_INPUT to jsonData,
                    KEY_PASSWORD_HASH to passwordHash
                )
            )
            .addTag(backupRestoreWorkerTag)
            .setConstraints(buildConstraints())
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
        workManager.cancelAllWorkByTag(backupRestoreWorkerTag)
    }

    fun getBackupIntervalFromWorkInfo(info: WorkInfo): BackupInterval? {
        val intervalTagIndex = info.tags
            .indexOfFirst { it.startsWith(WORK_INTERVAL_TAG_PREFIX) }

        return info.tags.elementAtOrNull(intervalTagIndex)
            ?.removePrefix(WORK_INTERVAL_TAG_PREFIX)
            ?.let { BackupInterval.valueOf(it) }
    }

    private fun buildConstraints(
        requiresBatteryNotLow: Boolean = true
    ): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(requiresBatteryNotLow)
        .setRequiresStorageNotLow(true)
        .build()
}