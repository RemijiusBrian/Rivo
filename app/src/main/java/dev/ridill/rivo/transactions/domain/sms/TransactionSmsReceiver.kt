package dev.ridill.rivo.transactions.domain.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TransactionSmsReceiver : BroadcastReceiver() {

    @Inject
    lateinit var service: TransactionSmsService

    override fun onReceive(context: Context, intent: Intent) {
        if (!service.isSmsActionValid(intent.action)) return
        service.saveTransactionsFromSMSData(intent)
    }
}