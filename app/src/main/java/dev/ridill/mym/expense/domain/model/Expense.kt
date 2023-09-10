package dev.ridill.mym.expense.domain.model

import dev.ridill.mym.core.data.db.MYMDatabase
import dev.ridill.mym.core.domain.util.DateUtil
import java.time.LocalDateTime

data class Expense(
    val id: Long,
    val amount: String,
    val note: String,
    val createdTimestamp: LocalDateTime,
    val tagId: Long?,
    val excluded: Boolean
) {
    companion object {
        val DEFAULT = Expense(
            id = MYMDatabase.DEFAULT_ID_LONG,
            amount = "",
            note = "",
            createdTimestamp = DateUtil.now(),
            tagId = null,
            excluded = false
        )
    }
}