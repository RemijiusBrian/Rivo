package dev.ridill.rivo.transactionSchedules.domain.scheduleReminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.logI
import dev.ridill.rivo.transactionSchedules.domain.model.Schedule

class AlarmManagerScheduleReminder(
    private val context: Context
) : ScheduleReminder {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun setReminder(schedule: Schedule) {
        val timeMillis = schedule.nextReminderDate?.let { DateUtil.toMillis(it) }
            ?: return
        val intent = Intent(context, ScheduleReminderReceiver::class.java).apply {
            action = ScheduleReminder.ACTION
            putExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, schedule.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            UtilConstants.pendingIntentFlags
        )

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC,
            timeMillis,
            pendingIntent
        )

        logI { "Transaction id ${schedule.id} scheduled for ${DateUtil.fromMillis(timeMillis)}" }
    }

    override fun cancel(transaction: Schedule) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                transaction.id.hashCode(),
                Intent(context, ScheduleReminderReceiver::class.java),
                UtilConstants.pendingIntentFlags
            )
        )
    }
}