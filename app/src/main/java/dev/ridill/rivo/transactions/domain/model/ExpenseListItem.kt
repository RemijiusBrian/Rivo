package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate

data class ExpenseListItem(
    val id: Long,
    val note: String,
    val amount: String,
    val date: LocalDate,
    val tag: ExpenseTag?,
    val excluded: Boolean
)