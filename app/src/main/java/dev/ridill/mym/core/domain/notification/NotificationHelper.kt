package dev.ridill.mym.core.domain.notification

interface NotificationHelper {
    fun registerChannelGroup()
    fun registerChannel()
    fun postNotification(
        id: Int,
        title: String,
        content: String
    )

    object Groups {
        const val EXPENSES = "EXPENSE_NOTIFICATIONS"
    }
}