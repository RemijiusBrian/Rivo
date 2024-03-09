package dev.ridill.rivo.scheduledTransaction.domain.notification

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
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.ui.navigation.destinations.AddEditTransactionScreenSpec
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction

class ScheduledTransactionNotificationHelper(
    private val context: Context
) : NotificationHelper<ScheduledTransaction> {
    private val notificationManager = NotificationManagerCompat.from(context)

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_SCHEDULED_TRANSACTIONS"

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.transactions(context))
            .setName(context.getString(R.string.notification_channel_group_transactions_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_scheduled_transactions_name))
            .setGroup(NotificationHelper.Groups.transactions(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)

    @SuppressLint("MissingPermission")
    override fun postNotification(id: Int, data: ScheduledTransaction) {
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.scheduled_transaction))
            .setContentText(
                context.getString(
                    R.string.you_have_a_payment_of_amount_due_today,
                    data.amount.toString()
//            TextFormat.currency(amount = data.amount, currency = currency)
                )
            )
            .setContentIntent(buildContentIntent(id))
            .build()

        /*val summaryNotification = buildBaseNotification()
            .setGroupSummary(true)
            .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_SUMMARY)
            .build()*/

        notificationManager.notify(id, notification)
    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildContentIntent(id: Int): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            AddEditTransactionScreenSpec.buildAutoAddedTransactionDeeplinkUri(id.toLong()), // FIXME: Change URI for appropriate screen
            context,
            RivoActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(id, UtilConstants.pendingIntentFlags)
        }
    }
}