package dev.ridill.rivo.core.domain.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dev.ridill.rivo.transactions.domain.notification.DeleteExpenseActionReceiver
import dev.ridill.rivo.transactions.domain.notification.MarkExcludedActionReceiver
import dev.ridill.rivo.transactions.domain.sms.ExpenseSmsReceiver

class ReceiverService(
    private val context: Context
) {

    fun toggleSmsReceiver(enable: Boolean) =
        toggleReceiver(ExpenseSmsReceiver::class.java, enable)

    fun toggleNotificationActionReceivers(enable: Boolean) {
        toggleReceiver(DeleteExpenseActionReceiver::class.java, enable)
        toggleReceiver(MarkExcludedActionReceiver::class.java, enable)
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