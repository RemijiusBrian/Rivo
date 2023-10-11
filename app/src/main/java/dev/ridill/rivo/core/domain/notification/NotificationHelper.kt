package dev.ridill.rivo.core.domain.notification

import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat

interface NotificationHelper {
    val channelId: String
    fun registerChannelGroup()

    fun registerChannel()

    fun buildBaseNotification(): NotificationCompat.Builder

    fun postNotification(id: Int, title: String, content: String?)

    fun dismissNotification(id: Int)

    object Groups {
        fun transactions(context: Context): String =
            "${context.packageName}.NOTIFICATION_CHANNEL_GROUP_TRANSACTIONS"

        fun others(context: Context): String =
            "${context.packageName}.NOTIFICATION_CHANNEL_GROUP_OTHERS"
    }

    object Utils {
        val pendingIntentFlags: Int
            get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        const val TIMEOUT_MILLIS = 5_000L
    }
}