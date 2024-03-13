package dev.ridill.rivo.transactionSchedules.domain.transactionScheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule

class AlarmManagerTransactionScheduler(
    private val context: Context
) : TransactionScheduler {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(transaction: TxSchedule) {
        val timeMillis = transaction.nextReminderDate?.let { DateUtil.toMillis(it) }
            ?: return
        val intent = Intent(context, ScheduleTriggerReceiver::class.java).apply {
            putExtra(TransactionScheduler.TX_ID, transaction.id)
        }
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

        logI { "Transaction id ${transaction.id} scheduled for ${DateUtil.fromMillis(timeMillis)}" }
    }

    override fun cancel(transaction: TxSchedule) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                transaction.id.hashCode(),
                Intent(context, ScheduleTriggerReceiver::class.java),
                UtilConstants.pendingIntentFlags
            )
        )
    }
}