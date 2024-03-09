package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.scheduledTransaction.domain.repository.ScheduledTransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionScheduleBootReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var repo: ScheduledTransactionRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        applicationScope.launch {
            repo.rescheduleAllTransactions()
        }
    }
}