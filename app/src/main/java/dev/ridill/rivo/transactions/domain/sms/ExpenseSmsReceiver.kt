package dev.ridill.rivo.transactions.domain.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExpenseSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var service: ExpenseSmsService

    override fun onReceive(context: Context, intent: Intent) {
        if (!service.isSmsActionValid(intent.action)) return
        service.saveExpenseFromSMSData(intent)
    }
}