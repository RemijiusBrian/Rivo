package dev.ridill.rivo.schedules.domain.model

import dev.ridill.rivo.core.domain.util.orZero
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

data class Schedule(
    val id: Long,
    val amount: Double,
    val note: String?,
    val type: TransactionType,
    val tagId: Long?,
    val folderId: Long?,
    val repeatMode: ScheduleRepeatMode,
    val nextReminderDate: LocalDateTime?,
    val lastPaidDate: LocalDateTime?
) {
    companion object {
        fun fromTransaction(
            transaction: Transaction,
            repeatMode: ScheduleRepeatMode
        ): Schedule = Schedule(
            id = transaction.id,
            amount = transaction.amount.toDoubleOrNull().orZero(),
            note = transaction.note.ifEmpty { null },
            type = transaction.type,
            repeatMode = repeatMode,
            tagId = transaction.tagId,
            folderId = transaction.folderId,
            nextReminderDate = transaction.timestamp,
            lastPaidDate = null
        )
    }
}