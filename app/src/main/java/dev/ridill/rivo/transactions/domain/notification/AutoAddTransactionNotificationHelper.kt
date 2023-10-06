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
import dev.ridill.rivo.R
import dev.ridill.rivo.application.RivoActivity
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.ui.navigation.destinations.ARG_TRANSACTION_ID
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTransactionScreenSpec

@SuppressLint("MissingPermission")
class AutoAddTransactionNotificationHelper(
    private val context: Context
) : NotificationHelper {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_TRANSACTION_AUTO_ADD"

    private val summaryId: String
        get() = "${context.packageName}.AUTO_ADDED_TRANSACTIONS_SUMMARY"

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat.Builder(NotificationHelper.Groups.TRANSACTIONS)
            .setName(context.getString(R.string.notification_channel_group_transactions_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_auto_add_transactions_name))
            .setGroup(NotificationHelper.Groups.TRANSACTIONS)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setGroup(summaryId)

    override fun postNotification(id: Int, title: String, content: String?) {
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = buildBaseNotification()
            .setContentTitle(title)
            .apply {
                if (!content.isNullOrEmpty()) {
                    setContentText(content)
                }
            }
            .setContentIntent(buildContentIntent(id))
            .addAction(buildDeleteAction(id))
            .addAction(buildMarkExcludedAction(id))
            .build()

        val summaryNotification = buildBaseNotification()
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()

        with(notificationManager) {
            notify(id, notification)
            notify(SUMMARY_ID, summaryNotification)
        }
    }

    fun updateNotificationToTransactionDeleted(id: Int) {
        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.transaction_deleted))
            .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
            .build()

        notificationManager.notify(id, notification)
    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }

    fun getSMSModelDownloadForegroundNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.setting_up_transaction_auto_add))
            .setProgress(100, 0, true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

    private fun buildContentIntent(id: Int): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            AddEditTransactionScreenSpec.buildAutoAddedTransactionDeeplinkUri(id.toLong()),
            context,
            RivoActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(id, NotificationHelper.Utils.pendingIntentFlags)
        }
    }

    private fun buildDeleteAction(id: Int): NotificationCompat.Action {
        val intent = Intent(context, DeleteTransactionActionReceiver::class.java).apply {
            putExtra(ARG_TRANSACTION_ID, id.toLong())
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
        val intent = Intent(context, MarkTransactionExcludedActionReceiver::class.java).apply {
            putExtra(ARG_TRANSACTION_ID, id.toLong())
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

private const val SUMMARY_ID = Int.MAX_VALUE - 1