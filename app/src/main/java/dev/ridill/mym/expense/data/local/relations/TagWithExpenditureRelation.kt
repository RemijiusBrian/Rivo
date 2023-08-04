package dev.ridill.mym.expense.data.local.relations

import java.time.LocalDateTime

data class TagWithExpenditureRelation(
    val name: String,
    val colorCode: Int,
    val createdTimestamp: LocalDateTime,
    val amount: Double
)