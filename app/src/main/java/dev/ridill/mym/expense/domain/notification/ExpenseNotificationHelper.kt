package dev.ridill.mym.expense.domain.notification

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationChannelGroupCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.notification.BaseNotificationHelper

class ExpenseNotificationHelper(
    private val context: Context
) : BaseNotificationHelper(context) {
    override fun registerChannelGroup() {
        val group = NotificationChannelGroupCompat.Builder(Groups.EXPENSES)
            .setName(context.getString(R.string.notification_channel_group_name_expenses))
            .build()
        notificationManager.createNotificationChannelGroup(group)
    }

    override fun registerChannel() {
        val channel = NotificationChannelCompat
            .Builder(ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
            .setName(context.getString(R.string.notification_channel_name_auto_add_expenses))
            .setGroup(Groups.EXPENSES)
            .build()
        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    override fun postNotification(id: Int, title: String, content: String) {
        if (!notificationManager.areNotificationsEnabled()) return

        val notification = NotificationCompat.Builder(context, ID)
            .setContentTitle(title)
            .setContentText(content)
            .build()

        notificationManager.notify(id, notification)
    }
}

private const val ID = "AUTO_ADD_EXPENSE_NOTIFICATION_CHANNEL"