package dev.ridill.mym.core.domain.notification

import android.app.PendingIntent
import androidx.core.app.NotificationCompat

interface NotificationHelper {
    fun registerChannelGroup()

    fun registerChannel()

    fun buildBaseNotification(): NotificationCompat.Builder

    fun postNotification(id: Int, title: String, content: String?)

    fun dismissNotification(id: Int)

    object Groups {
        const val EXPENSES = "dev.ridill.mym.CHANNEL_GROUP_EXPENSE_NOTIFICATIONS"
    }

    object Utils {
        val pendingIntentFlags: Int
            get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        const val TIMEOUT_MILLIS = 2000L
    }
}