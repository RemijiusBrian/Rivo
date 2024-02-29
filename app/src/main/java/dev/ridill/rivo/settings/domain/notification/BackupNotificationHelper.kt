package dev.ridill.rivo.settings.domain.notification

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import dev.ridill.rivo.R
import dev.ridill.rivo.application.RivoActivity
import dev.ridill.rivo.core.domain.notification.NotificationHelper
import dev.ridill.rivo.core.ui.navigation.destinations.BackupSettingsScreenSpec

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
            .Builder(NotificationHelper.Groups.others(context))
            .setName(context.getString(R.string.notification_channel_group_others_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    private fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_backups_name))
            .setGroup(NotificationHelper.Groups.others(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    fun buildForegroundNotification(
        @StringRes titleRes: Int
    ): NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(R.drawable.ic_notification)
        .setContentTitle(context.getString(titleRes))
        .setProgress(100, 0, true)
        .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)

    @SuppressLint("MissingPermission")
    fun showBackupErrorNotification(
        @StringRes contentRes: Int
    ) {
        if (!notificationManager.areNotificationsEnabled()) return
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.error_backup_failed_notification_title))
            .setContentText(context.getString(contentRes))
            .setContentIntent(buildContentIntent())
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(context.getString(contentRes))
                    .bigText(context.getString(R.string.tap_to_resolve_issues))
            )
            .setAutoCancel(true)
            .build()

        notificationManager.notify(BACKUP_FAILED_NOTIFICATION_ID, notification)
    }

    private fun buildContentIntent(): PendingIntent? {
        val intent = Intent(
            Intent.ACTION_VIEW,
            BackupSettingsScreenSpec.buildBackupSettingsDeeplinkUri(),
            context,
            RivoActivity::class.java
        )
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(1, NotificationHelper.Utils.pendingIntentFlags)
        }
    }

    private val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_BACKUPS"
}

private const val BACKUP_FAILED_NOTIFICATION_ID = 2