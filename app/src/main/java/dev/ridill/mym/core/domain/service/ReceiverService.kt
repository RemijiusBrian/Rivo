package dev.ridill.mym.core.domain.service

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import dev.ridill.mym.expense.domain.sms.ExpenseSmsReceiver

class ReceiverService(
    private val context: Context
) {

    fun toggleSmsReceiver(enable: Boolean) {
        val receiver = ComponentName(context, ExpenseSmsReceiver::class.java)
        val newState = if (enable) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        context.packageManager.setComponentEnabledSetting(
            receiver,
            newState,
            PackageManager.DONT_KILL_APP
        )
    }
}