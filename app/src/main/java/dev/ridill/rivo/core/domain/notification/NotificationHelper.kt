package dev.ridill.rivo.core.domain.notification

import android.app.PendingIntent
import androidx.core.app.NotificationCompat

interface NotificationHelper {
    fun registerChannelGroup()

    fun registerChannel()

    fun buildBaseNotification(): NotificationCompat.Builder

    fun postNotification(id: Int, title: String, content: String?)

    fun dismissNotification(id: Int)

    object Groups {
        const val TRANSACTIONS = "dev.ridill.mym.CHANNEL_GROUP_TRANSACTIONS"
        const val OTHERS = "dev.ridill.mym.CHANNEL_GROUP_OTHERS"
    }

    object Utils {
        val pendingIntentFlags: Int
            get() = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE

        const val TIMEOUT_MILLIS = 5_000L
    }
}