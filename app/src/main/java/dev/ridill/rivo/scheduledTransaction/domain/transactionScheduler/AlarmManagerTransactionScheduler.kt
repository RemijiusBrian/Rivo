package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction

class AlarmManagerTransactionScheduler(
    private val context: Context
) : TransactionScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(transaction: ScheduledTransaction) {
        val intent = Intent(context, ScheduledTransactionReceiver::class.java).apply {
            putExtra(TransactionScheduler.TX_ID, transaction.id)
        }
        val timeMillis = DateUtil.toMillis(transaction.nextPaymentDate)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            transaction.id.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            timeMillis,
            pendingIntent
        )
    }

    override fun cancel(transaction: ScheduledTransaction) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                transaction.id.hashCode(),
                Intent(context, ScheduledTransactionReceiver::class.java),
                UtilConstants.pendingIntentFlags
            )
        )
    }
}