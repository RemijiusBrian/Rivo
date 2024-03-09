package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.scheduledTransaction.domain.model.TransactionRepeatMode
import dev.ridill.rivo.scheduledTransaction.domain.notification.ScheduledTransactionNotificationHelper
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
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
    lateinit var notificationHelper: ScheduledTransactionNotificationHelper

    @Inject
    lateinit var currencyRepo: CurrencyRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(TransactionScheduler.TX_ID, -1L)
            ?.takeIf { it > -1L }
            ?: return
        applicationContext.launch {
            val transaction = repo.getTransactionById(id) ?: return@launch
            val currency = currencyRepo.getCurrencyForDateOrNext().first()
            notificationHelper.postNotification(
                id = transaction.id.hashCode(),
                title = context?.getString(R.string.scheduled_transaction).orEmpty(),
                content = context?.getString(
                    R.string.you_have_a_payment_of_amount_due_today,
                    TextFormat.currency(amount = transaction.amount, currency = currency)
                )
            )
            val monthsToAdd = when (transaction.repeatMode) {
                TransactionRepeatMode.ONE_TIME -> return@launch // Does not need to be rescheduled
                TransactionRepeatMode.MONTHLY -> 1L
                TransactionRepeatMode.BI_MONTHLY -> 2L
            }
            val nextPaymentDate = transaction.nextPaymentDate.plusMonths(monthsToAdd)
            repo.updateNextPaymentDateForTransactionById(id, nextPaymentDate)
            repo.scheduleTransaction(transaction.copy(nextPaymentDate = nextPaymentDate))
        }
    }
}