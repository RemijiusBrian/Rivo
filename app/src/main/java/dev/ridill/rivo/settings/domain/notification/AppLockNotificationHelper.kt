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
import dev.ridill.rivo.core.domain.util.UtilConstants
import kotlin.random.Random

class AppLockNotificationHelper(
    private val context: Context
) : NotificationHelper<Unit> {
    private val notificationManager = NotificationManagerCompat.from(context)

    override val channelId: String
        get() = "${context.packageName}.NOTIFICATION_CHANNEL_APP_LOCK"

    init {
        registerChannelGroup()
        registerChannel()
    }

    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat
            .Builder(NotificationHelper.Groups.others(context))
            .setName(context.getString(R.string.notification_channel_group_others_name))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(channelId, NotificationManagerCompat.IMPORTANCE_LOW)
            .setName(context.getString(R.string.notification_channel_app_lock_name))
            .setGroup(NotificationHelper.Groups.others(context))
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    override fun buildBaseNotification(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_ic_lock_open)
            .setContentIntent(buildContentIntent())
            .addAction(buildLockAction())

    private fun buildContentIntent(): PendingIntent? {
        val intent = Intent(context, RivoActivity::class.java)
        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(Random.nextInt(), UtilConstants.pendingIntentFlags)
        }
    }

    private fun buildLockAction(): NotificationCompat.Action {
        val intent = Intent(context, LockAppImmediateReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, Random.nextInt(), intent, UtilConstants.pendingIntentFlags
        )

        return NotificationCompat.Action.Builder(
            R.drawable.notification_ic_lock_open,
            context.getString(R.string.lock_now),
            pendingIntent
        ).build()
    }
}