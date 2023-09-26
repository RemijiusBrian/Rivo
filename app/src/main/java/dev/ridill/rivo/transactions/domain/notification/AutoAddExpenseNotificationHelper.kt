package dev.ridill.rivo.transactions.domain.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import dev.ridill.rivo.R
import dev.ridill.rivo.application.RivoActivity
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.ui.navigation.destinations.ARG_EXPENSE_ID

@SuppressLint("MissingPermission")
class AutoAddExpenseNotificationHelper(
    private val context: Context
) : NotificationHelper {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat.Builder(NotificationHelper.Groups.EXPENSES)
            .setName(context.getString(R.string.notification_channel_group_expenses_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_auto_add_expenses_name))
            .setGroup(NotificationHelper.Groups.EXPENSES)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setGroup(SUMMARY_GROUP)

    override fun postNotification(id: Int, title: String, content: String?) {
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = buildBaseNotification()
            .setContentTitle(title)
            .apply {
                if (!content.isNullOrEmpty()) {
                    setContentText(content)
                }
            }
            .setAutoCancel(true)
            .setContentIntent(buildContentIntent(id))
            .addAction(buildDeleteAction(id))
            .addAction(buildMarkExcludedAction(id))
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()

        val summaryNotification = buildBaseNotification()
            .setGroupSummary(true)
            .setAutoCancel(true)
            .build()

        with(notificationManager) {
            notify(id, notification)
            notify(SUMMARY_ID, summaryNotification)
        }
    }

    fun updateNotificationToExpenseDeleted(id: Int) {
        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.expense_deleted))
            .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
            .build()

        notificationManager.notify(id, notification)
    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildContentIntent(id: Int): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "https://www.mym.com/expense/$id".toUri(),
            context,
            RivoActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(id, NotificationHelper.Utils.pendingIntentFlags)
        }
    }

    private fun buildDeleteAction(id: Int): NotificationCompat.Action {
        val intent = Intent(context, DeleteExpenseActionReceiver::class.java).apply {
            putExtra(ARG_EXPENSE_ID, id.toLong())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, NotificationHelper.Utils.pendingIntentFlags
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.action_delete),
            pendingIntent
        ).build()
    }

    private fun buildMarkExcludedAction(id: Int): NotificationCompat.Action {
        val intent = Intent(context, MarkExcludedActionReceiver::class.java).apply {
            putExtra(ARG_EXPENSE_ID, id.toLong())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent, NotificationHelper.Utils.pendingIntentFlags
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.action_mark_excluded),
            pendingIntent
        ).build()
    }
}

private const val CHANNEL_ID = "dev.ridill.mym.CHANNEL_AUTO_ADD_EXPENSE_NOTIFICATIONS"
private const val SUMMARY_GROUP = "dev.ridill.mym.AUTO_ADDED_EXPENSES"
private const val SUMMARY_ID = Int.MAX_VALUE - 1