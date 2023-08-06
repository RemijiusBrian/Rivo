package dev.ridill.mym.core.domain.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat

abstract class BaseNotificationHelper(context: Context) {
    val notificationManager = NotificationManagerCompat.from(context)

    init {
        this.registerChannelGroup()
        this.registerChannel()
    }

    abstract fun registerChannelGroup()
    abstract fun registerChannel()
    abstract fun postNotification(
        id: Int,
        title: String,
        content: String
    )

    object Groups {
        const val EXPENSES = "EXPENSE_NOTIFICATIONS"
    }
}