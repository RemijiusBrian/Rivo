package dev.ridill.mym.dashboard.domain.model

import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

data class RecentTransaction(
    val id: Long,
    val note: String,
    val amount: String,
    val dateTime: LocalDateTime
) {
    val dayOfWeek: String
        get() = dateTime.dayOfWeek
            .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
            .uppercase()

    val dayOfMonth: String
        get() = dateTime.dayOfMonth.toString()
}