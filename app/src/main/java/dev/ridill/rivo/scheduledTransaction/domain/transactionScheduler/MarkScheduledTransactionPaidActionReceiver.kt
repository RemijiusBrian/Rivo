package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.scheduledTransaction.data.toTransaction
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.notification.SCHEDULED_TX_ID
import dev.ridill.rivo.scheduledTransaction.domain.notification.ScheduledTransactionNotificationHelper
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkScheduledTransactionPaidActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: ScheduledTransactionRepository

    @Inject
    lateinit var addEditTxRepo: AddEditTransactionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<ScheduledTransaction>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ScheduledTransactionNotificationHelper.ACTION_MARK_SCHEDULED_TX_PAID)
            return

        applicationScope.launch {
            val txId = intent.getLongExtra(SCHEDULED_TX_ID, -1L)
                .takeIf { it > -1L }
                ?: return@launch
            val transaction = repo.getTransactionById(txId)
                ?.toTransaction()
                ?: return@launch

            addEditTxRepo.saveTransaction(transaction)
            notificationHelper.updateNotification(
                id = txId.hashCode(),
                notification = notificationHelper
                    .buildBaseNotification()
                    .setContentTitle(context?.getString(R.string.transaction_added))
                    .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
                    .build()
            )
        }
    }
}