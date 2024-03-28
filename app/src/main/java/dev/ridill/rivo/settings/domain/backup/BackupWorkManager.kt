package dev.ridill.rivo.settings.domain.backup

import android.content.Context
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
import dev.ridill.rivo.core.domain.util.toUUID
import dev.ridill.rivo.settings.domain.modal.BackupDetails
import dev.ridill.rivo.settings.domain.modal.BackupInterval
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class BackupWorkManager(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    private val dataBackupWorkerTag: String
        get() = "${context.packageName}.DATA_BACKUP_WORKER_TAG"

    private val oneTimeBackupWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_BACKUP_WORK"

    private val periodicBackupWorkName: String
        get() = "${context.packageName}.PERIODIC_DATA_BACKUP_WORK"

    private val dataRestoreWorkerTag: String
        get() = "${context.packageName}.DATA_RESTORE_WORKER_TAG"

    private val oneTimeDataDownloadWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_DOWNLOAD_WORK"

    private val oneTimeRestoreWorkName: String
        get() = "${context.packageName}.ONE_TIME_DATA_RESTORE_WORK"

    companion object {
        const val WORK_INTERVAL_TAG_PREFIX = "WORK_INTERVAL-"
        const val KEY_MESSAGE = "KEY_MESSAGE"
        const val KEY_BACKUP_FILE_ID = "KEY_BACKUP_FILE_ID"
        const val KEY_BACKUP_TIMESTAMP = "KEY_BACKUP_DATE"
        const val KEY_PASSWORD_HASH = "KEY_PASSWORD_HASH"

        const val RESTORE_WORKER_NOTIFICATION_ID = "RESTORE_WORKER_NOTIFICATION"
        const val BACKUP_WORKER_NOTIFICATION_ID = "BACKUP_WORKER_NOTIFICATION"

        private const val BACKOFF_DELAY_MINUTES = 10L
    }

    fun schedulePeriodicBackupWork(interval: BackupInterval) {
        val workRequest = PeriodicWorkRequestBuilder<GDriveDataBackupWorker>(
            interval.daysInterval,
            TimeUnit.DAYS
        )
            .setConstraints(buildBackupConstraints())
            .setId(periodicBackupWorkName.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${interval.name}")
            .addTag(dataBackupWorkerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
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
        .getWorkInfoByIdFlow(periodicBackupWorkName.toUUID())

    fun cancelPeriodicBackupWork() {
        workManager.cancelWorkById(periodicBackupWorkName.toUUID())
    }

    fun runImmediateBackupWork() {
        val workRequest = OneTimeWorkRequestBuilder<GDriveDataBackupWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .setId(oneTimeBackupWorkName.toUUID())
            .addTag("$WORK_INTERVAL_TAG_PREFIX${BackupInterval.MANUAL.name}")
            .addTag(dataBackupWorkerTag)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BACKOFF_DELAY_MINUTES,
                TimeUnit.MINUTES
            )
            .build()

        workManager.enqueueUniqueWork(
            oneTimeBackupWorkName,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun getImmediateBackupWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeBackupWorkName.toUUID())

    fun runImmediateRestoreWork(backupDetails: BackupDetails, passwordHash: String) {
        val dataDownloadWorkRequest = OneTimeWorkRequestBuilder<GDriveDataDownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .addTag(dataRestoreWorkerTag)
            .setId(oneTimeDataDownloadWorkName.toUUID())
            .setInputData(
                workDataOf(
                    KEY_BACKUP_FILE_ID to backupDetails.id,
                    KEY_BACKUP_TIMESTAMP to backupDetails.timestamp
                )
            )
            .build()
        val dataRestoreWorkRequest = OneTimeWorkRequestBuilder<GDriveDataRestoreWorker>()
            .setExpedited(OutOfQuotaPolicy.DROP_WORK_REQUEST)
            .addTag(dataRestoreWorkerTag)
            .setId(oneTimeRestoreWorkName.toUUID())
            .setInputData(
                workDataOf(
                    KEY_PASSWORD_HASH to passwordHash
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        workManager.beginUniqueWork(
            oneTimeRestoreWorkName,
            ExistingWorkPolicy.REPLACE,
            dataDownloadWorkRequest
        )
            .then(dataRestoreWorkRequest)
            .enqueue()
    }

    fun getRestoreDataDownloadWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeDataDownloadWorkName.toUUID())

    fun getImmediateDataRestoreWorkInfoFlow(): Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(oneTimeRestoreWorkName.toUUID())

    fun cancelAllWorks() {
        workManager.cancelAllWork()
    }

    fun getBackupIntervalFromWorkInfo(info: WorkInfo): BackupInterval? {
        val intervalTag = info.tags.find { it.startsWith(WORK_INTERVAL_TAG_PREFIX) }

        return intervalTag
            ?.removePrefix(WORK_INTERVAL_TAG_PREFIX)
            ?.let { BackupInterval.valueOf(it) }
    }

    private fun buildBackupConstraints(): Constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()
}