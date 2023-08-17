package dev.ridill.mym.settings.domain.notification

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.notification.NotificationHelper

class BackupNotificationHelper(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    private fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.OTHERS)
            .setName(context.getString(R.string.notification_channel_group_name_others))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    private fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_name_backups))
            .setGroup(NotificationHelper.Groups.OTHERS)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun getForegroundNotification(
        @StringRes titleRes: Int
    ): NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(titleRes))
        .setProgress(100, 0, true)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_DEFAULT)

    private val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_BACKUPS"
}