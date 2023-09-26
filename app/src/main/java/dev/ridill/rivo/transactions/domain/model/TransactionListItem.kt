package dev.ridill.rivo.transactions.domain.model

import java.time.LocalDate

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: String,
    val date: LocalDate,
    val tag: TransactionTag?,
    val excluded: Boolean
)