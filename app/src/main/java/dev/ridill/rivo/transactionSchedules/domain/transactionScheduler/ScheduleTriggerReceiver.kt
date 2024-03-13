package dev.ridill.rivo.transactionSchedules.domain.transactionScheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule
import dev.ridill.rivo.transactionSchedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.transactionSchedules.domain.repository.SchedulesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleTriggerReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<TxSchedule>

    override fun onReceive(context: Context?, intent: Intent?) {
        val id = intent?.getLongExtra(TransactionScheduler.TX_ID, -1L)
            ?.takeIf { it > -1L }
            ?: return
        logI { "Schedule triggered at ${DateUtil.now()}" }
        applicationContext.launch {
            val transaction = repo.getScheduleById(id) ?: return@launch
            logI { "Schedule - $transaction" }
            notificationHelper.postNotification(
                id = transaction.id.hashCode(),
                data = transaction
            )
            val nextReminderDate = transaction.nextReminderDate?.let {
                when (transaction.repeatMode) {
                    ScheduleRepeatMode.NO_REPEAT -> null
                    ScheduleRepeatMode.WEEKLY -> it.plusWeeks(1)
                    ScheduleRepeatMode.MONTHLY -> it.plusMonths(1)
                    ScheduleRepeatMode.BI_MONTHLY -> it.plusMonths(2)
                    ScheduleRepeatMode.YEARLY -> it.plusYears(1)
                }
            }
            repo.saveAndSetSchedule(transaction.copy(nextReminderDate = nextReminderDate))
        }
    }
}