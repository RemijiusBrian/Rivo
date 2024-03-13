package dev.ridill.rivo.transactionSchedules.data.local.relation

import java.time.LocalDate
import java.time.LocalDateTime

data class ScheduleWithLastPaidDateRelation(
    val id: Long,
    val amount: Double,
    val note: String?,
    val nextReminderDate: LocalDate?,
    val lastPaymentTimestamp: LocalDateTime?
)