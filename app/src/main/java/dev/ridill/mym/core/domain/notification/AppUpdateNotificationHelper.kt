package dev.ridill.mym.core.domain.notification

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.ridill.mym.R

@SuppressLint("MissingPermission")
class AppUpdateNotificationHelper(
    private val context: Context
) : NotificationHelper {

    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat.Builder(NotificationHelper.Groups.OTHERS)
            .setName(context.getString(R.string.notification_channel_group_name_others))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat.Builder(
            CHANNEL_ID,
            NotificationManagerCompat.IMPORTANCE_DEFAULT
        )
            .setName(
                context.getString(
                    com.google.firebase.appdistribution.impl.R.string.app_update_notification_channel_name
                )
            )
            .setDescription(
                context.getString(
                    com.google.firebase.appdistribution.impl.R.string.app_update_notification_channel_description
                )
            )
            .setGroup(NotificationHelper.Groups.OTHERS)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)

    fun postUpdateNotification(id: Int) {
        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.app_update_in_progress))
            .setProgress(100, 100, true)
            .build()
        notificationManager.notify(id, notification)
    }

    fun postUpdateFailedNotification(id: Int) {
        val notification = buildBaseNotification()
            .setContentTitle(context.getString(R.string.update_failed))
            .setTimeoutAfter(NotificationHelper.Utils.TIMEOUT_MILLIS)
            .build()
        notificationManager.notify(id, notification)
    }

    override fun postNotification(id: Int, title: String, content: String?) {
    }

    override fun dismissNotification(id: Int) {
        notificationManager.cancel(id)
    }
}

private const val CHANNEL_ID = "dev.ridill.mym.CHANNEL_APP_UPDATES"