package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduledTransactionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var repo: ScheduledTransactionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<ScheduledTransaction>

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(TransactionScheduler.TX_ID, -1L)
            ?.takeIf { it > -1L }
            ?: return
        applicationContext.launch {
            val transaction = repo.getTransactionById(id) ?: return@launch
            notificationHelper.postNotification(
                id = transaction.id.hashCode(),
                data = transaction
            )
        }
    }
}