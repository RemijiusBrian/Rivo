package dev.ridill.rivo.scheduledTransaction.domain.model

import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate

data class ScheduledTransaction(
    val id: Long,
    val amount: Double,
    val note: String?,
    val type: TransactionType,
    val repeatMode: TransactionRepeatMode,
    val nextReminderDate: LocalDate?
)