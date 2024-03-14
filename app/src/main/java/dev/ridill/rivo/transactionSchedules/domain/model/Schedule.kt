package dev.ridill.rivo.transactionSchedules.domain.model

import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDate

data class Schedule(
    val id: Long,
    val amount: Double,
    val note: String?,
    val type: TransactionType,
    val tagId: Long?,
    val folderId: Long?,
    val repeatMode: ScheduleRepeatMode,
    val nextReminderDate: LocalDate?
)