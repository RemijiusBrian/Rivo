package dev.ridill.mym.expense.data.local.relations

import java.time.LocalDateTime

data class ExpenseWithTagRelation(
    val expenseId: Long,
    val expenseNote: String,
    val expenseAmount: Double,
    val expenseTimestamp: LocalDateTime,
    val tagId: Long?,
    val tagName: String?,
    val tagColorCode: Int?,
    val tagCreatedTimestamp: LocalDateTime?,
    val isExcludedTransaction: Boolean
)