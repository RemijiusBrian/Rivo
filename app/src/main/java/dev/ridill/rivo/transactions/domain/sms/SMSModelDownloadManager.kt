package dev.ridill.rivo.transactions.domain.sms

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager

class SMSModelDownloadManager(
    private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    private val workName: String
        get() = "${context.packageName}.SMS_MODEL_DOWNLOAD_WORK"

    fun downloadSMSModelIfNeeded() {
        val workRequest = OneTimeWorkRequestBuilder<SMSModelDownloadWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, workRequest)
    }
}