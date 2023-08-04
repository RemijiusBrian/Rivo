package dev.ridill.mym.expense.domain.model

import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.DateUtil
import java.time.LocalDateTime

data class Expense(
    val id: Long,
    val amount: String,
    val note: String,
    val dateTime: LocalDateTime
) {
    companion object {
        val DEFAULT = Expense(
            id = MYMDatabase.DEFAULT_ID_LONG,
            amount = "",
            note = "",
            dateTime = DateUtil.now()
        )
    }
}