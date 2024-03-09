package dev.ridill.rivo.transactions.domain.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.notification.NotificationHelper

class AutoAddTxSetupNotificationHelper(
    private val context: Context
) : NotificationHelper<Unit> {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_TRANSACTION_AUTO_ADD_SETUP"

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.transactions(context))
            .setName(context.getString(R.string.notification_channel_group_transactions_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_auto_add_transaction_setup_name))
            .setGroup(NotificationHelper.Groups.transactions(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
}