package dev.ridill.mym.dashboard.domain.model

import dev.ridill.mym.expense.domain.model.ExpenseTag
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale

data class RecentSpend(
    val id: Long,
    val note: String,
    val amount: String,
    val dateTime: LocalDateTime,
    val tag: ExpenseTag?
) {
    val dayOfWeek: String
        get() = dateTime.dayOfWeek
            .getDisplayName(TextStyle.SHORT_STANDALONE, Locale.getDefault())
            .uppercase()

    val dayOfMonth: String
        get() = dateTime.dayOfMonth.toString()
}