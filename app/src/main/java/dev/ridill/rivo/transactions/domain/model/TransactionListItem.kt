package dev.ridill.rivo.transactions.domain.model

import dev.ridill.rivo.transactionGroups.domain.model.TxGroup
import java.time.LocalDate

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: Double,
    val date: LocalDate,
    val direction: TransactionDirection,
    val tag: TransactionTag?,
    val group: TxGroup?,
    val excluded: Boolean
)