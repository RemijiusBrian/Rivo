package dev.ridill.rivo.transactionSchedules.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.transactionSchedules.data.toTransaction
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import dev.ridill.rivo.transactionSchedules.domain.notification.SCHEDULED_TX_ID
import dev.ridill.rivo.transactionSchedules.domain.notification.TxScheduleNotificationHelper
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MarkScheduleAsPaidActionReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var addEditTxRepo: AddEditTransactionRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<TxSchedule>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != TxScheduleNotificationHelper.ACTION_MARK_SCHEDULED_TX_PAID)
            return

        applicationScope.launch {
            val txId = intent.getLongExtra(SCHEDULED_TX_ID, -1L)
                .takeIf { it > -1L }
                ?: return@launch
            val transaction = repo.getScheduleById(txId)
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