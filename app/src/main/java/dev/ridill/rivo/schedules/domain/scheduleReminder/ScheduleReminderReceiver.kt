package dev.ridill.rivo.schedules.domain.scheduleReminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.di.ApplicationScope
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.model.ScheduleRepeatMode
import dev.ridill.rivo.schedules.domain.repository.SchedulesRepository
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleReminderReceiver : BroadcastReceiver() {

    @ApplicationScope
    @Inject
    lateinit var applicationContext: CoroutineScope

    @Inject
    lateinit var currencyRepo: CurrencyRepository

    @Inject
    lateinit var repo: SchedulesRepository

    @Inject
    lateinit var notificationHelper: NotificationHelper<Schedule>

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != ScheduleReminder.ACTION) return
        val id = intent.getLongExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, -1L)
            .takeIf { it > -1L }
            ?: return
        logI { "Schedule triggered at ${DateUtil.now()}" }
        applicationContext.launch {
            val schedule = repo.getScheduleById(id)
                ?: return@launch
            logI { "Schedule - $schedule" }
            notificationHelper.postNotification(
                id = schedule.id.hashCode(),
                data = schedule
            )
            val nextReminderDate = schedule.nextReminderDate?.let {
                when (schedule.repeatMode) {
                    ScheduleRepeatMode.NO_REPEAT -> null
                    ScheduleRepeatMode.WEEKLY -> it.plusWeeks(1)
                    ScheduleRepeatMode.MONTHLY -> it.plusMonths(1)
                    ScheduleRepeatMode.BI_MONTHLY -> it.plusMonths(2)
                    ScheduleRepeatMode.YEARLY -> it.plusYears(1)
                }
            }
            repo.saveScheduleAndSetReminder(schedule.copy(nextReminderDate = nextReminderDate))
        }
    }
}