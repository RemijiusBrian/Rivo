package dev.ridill.rivo.transactions.domain.sms

import android.content.Context
import android.content.pm.ServiceInfo
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.core.domain.util.tryOrNull
import dev.ridill.rivo.transactions.domain.notification.AutoAddTransactionNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random

@HiltWorker
class SMSModelDownloadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val smsService: TransactionSmsService,
    private val notificationHelper: AutoAddTransactionNotificationHelper
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        startForeground()
        logI { "Downloading ML Kit Model" }
        tryOrNull {
            smsService.downloadModelIfNeeded()
            logI { "ML Kit Model Downloaded" }
            Result.success()
        } ?: Result.failure()
    }

    private suspend fun startForeground() {
        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                notificationHelper.getSMSModelDownloadForegroundNotification().build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        )
    }
}