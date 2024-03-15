package dev.ridill.rivo.schedules.domain.notification

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import dev.ridill.rivo.R
import dev.ridill.rivo.application.RivoActivity
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.ui.navigation.destinations.SchedulesAndPlansListScreenSpec
import dev.ridill.rivo.schedules.domain.model.Schedule
import dev.ridill.rivo.schedules.domain.scheduleReminder.MarkScheduleAsPaidActionReceiver
import dev.ridill.rivo.schedules.domain.scheduleReminder.ScheduleReminder

@SuppressLint("MissingPermission")
class ScheduleReminderNotificationHelper(
    private val context: Context
) : NotificationHelper<Schedule> {
    private val notificationManager = NotificationManagerCompat.from(context)

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_SCHEDULE_REMINDERS"

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.schedules(context))
            .setName(context.getString(R.string.notification_channel_group_schedules_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_schedule_reminders_name))
            .setGroup(NotificationHelper.Groups.transactions(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

    override fun postNotification(id: Int, data: Schedule) {
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.schedule_reminder))
            .setContentText(
                context.getString(
                    R.string.you_have_a_payment_of_amount_due_today,
                    data.amount.toString()
//            TextFormat.currency(amount = data.amount, currency = currency)
                )
            )
            .setContentIntent(buildContentIntent(data.id))
            .addAction(buildMarkPaidAction(data.id))
            .build()

        /*val summaryNotification = buildBaseNotification()
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()*/

        notificationManager.notify(id, notification)
    }

    override fun updateNotification(id: Int, notification: Notification) {
        notificationManager.notify(id, notification)

    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildContentIntent(id: Long): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            SchedulesAndPlansListScreenSpec.getViewDeeplinkUri(),
            context,
            RivoActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(id.hashCode(), UtilConstants.pendingIntentFlags)
        }
    }

    private fun buildMarkPaidAction(id: Long): NotificationCompat.Action {
        val intent = Intent(context, MarkScheduleAsPaidActionReceiver::class.java).apply {
            action = ACTION_MARK_SCHEDULED_AS_PAID
            putExtra(ScheduleReminder.EXTRA_SCHEDULE_ID, id)
        }
        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.mark_paid),
            PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                intent,
                UtilConstants.pendingIntentFlags
            )
        ).build()
    }

    companion object {
        const val ACTION_MARK_SCHEDULED_AS_PAID = "ACTION_MARK_SCHEDULED_AS_PAID"
    }
}