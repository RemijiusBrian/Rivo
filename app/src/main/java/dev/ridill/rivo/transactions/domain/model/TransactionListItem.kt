package dev.ridill.rivo.transactions.domain.model

import dev.ridill.rivo.transactionFolders.domain.model.TransactionFolder
import java.time.LocalDate

data class TransactionListItem(
    val id: Long,
    val note: String,
    val amount: Double,
    val date: LocalDate,
    val type: TransactionType,
    val tag: TransactionTag?,
    val folder: TransactionFolder?,
    val excluded: Boolean
)