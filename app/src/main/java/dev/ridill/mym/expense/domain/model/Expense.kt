package dev.ridill.mym.expense.domain.model

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.Zero
import java.time.LocalDateTime

data class Expense(
    val id: Long,
    val amount: String,
    val note: String,
    val dateTime: LocalDateTime
) {
    val timeFormatted: String
        get() = dateTime.format(DateUtil.Formatters.localizedTimeShort)

    val dateFormatted: String
        get() = dateTime.format(DateUtil.Formatters.ddth_MMM_spaceSep)

    companion object {
        val DEFAULT = Expense(
            id = Long.Zero,
            amount = "",
            note = "",
            dateTime = DateUtil.now()
        )
    }
}