package dev.ridill.rivo.settings.domain.notification

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

class AppLockNotificationHelper(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    private fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.others(context))
            .setName(context.getString(R.string.notification_channel_group_others_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    private fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_app_lock_name))
            .setGroup(NotificationHelper.Groups.others(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun getForegroundNotification(
    ): NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(
            context.getString(
                R.string.app_unlocked,
                context.getString(R.string.app_name)
            )
        )
        .setContentText(context.getString(R.string.tap_to_open))
        .setContentIntent(buildContentIntent())
        .addAction(buildLockAction())
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

    private fun buildContentIntent(): PendingIntent? {
        val intent = Intent(context, RivoActivity::class.java)
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(1, NotificationHelper.Utils.pendingIntentFlags)
        }
    }

    private fun buildLockAction(): NotificationCompat.Action {
        val intent = Intent(context, LockAppImmediateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 1, intent, NotificationHelper.Utils.pendingIntentFlags
        )

        return NotificationCompat.Action.Builder(
            R.drawable.ic_notification,
            context.getString(R.string.lock),
            pendingIntent
        ).build()
    }


    private val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_APP_LOCK"
}