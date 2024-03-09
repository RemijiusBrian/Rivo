package dev.ridill.rivo.core.domain.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.MarkScheduledTransactionPaidActionReceiver
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.TransactionScheduleBootReceiver
import dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler.TransactionScheduleTimeSetReceiver
import dev.ridill.rivo.settings.domain.notification.LockAppImmediateReceiver
import dev.ridill.rivo.transactions.domain.notification.DeleteTransactionActionReceiver
import dev.ridill.rivo.transactions.domain.notification.MarkTransactionExcludedActionReceiver
import dev.ridill.rivo.transactions.domain.sms.TransactionSmsReceiver

class ReceiverService(
    private val context: Context
) {
    fun toggleSmsReceiver(enable: Boolean) =
        toggleReceiver(TransactionSmsReceiver::class.java, enable)

    fun toggleNotificationActionReceivers(enable: Boolean) {
        toggleReceiver(DeleteTransactionActionReceiver::class.java, enable)
        toggleReceiver(MarkTransactionExcludedActionReceiver::class.java, enable)
        toggleReceiver(LockAppImmediateReceiver::class.java, enable)
        toggleReceiver(MarkScheduledTransactionPaidActionReceiver::class.java, enable)
    }

    fun enableBootAndTimeSetReceivers() {
        toggleReceiver(TransactionScheduleBootReceiver::class.java, true)
        toggleReceiver(TransactionScheduleTimeSetReceiver::class.java, true)
    }

    private fun toggleReceiver(receiverClass: Class<*>, enable: Boolean) {
        val componentName = ComponentName(context, receiverClass)
        val newState = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        context.packageManager.setComponentEnabledSetting(
            componentName,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}