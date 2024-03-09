package dev.ridill.rivo.scheduledTransaction.domain.model

import java.time.LocalDate

data class ScheduledTransaction(
    val id: Long,
    val amount: Double,
    val repeatMode: TransactionRepeatMode,
    val nextPaymentDate: LocalDate
)