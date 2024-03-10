package dev.ridill.rivo.scheduledTransaction.data

import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.scheduledTransaction.data.local.entity.ScheduledTransactionEntity
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.model.TransactionRepeatMode
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import java.time.LocalDateTime

fun ScheduledTransactionEntity.toScheduledTransaction(): ScheduledTransaction =
    ScheduledTransaction(
        id = id,
        repeatMode = TransactionRepeatMode.valueOf(repeatModeName),
        nextReminderDate = nextReminderDate,
        amount = amount,
        note = note,
        type = TransactionType.valueOf(typeName)
    )

fun ScheduledTransaction.toTransaction(
    dateTime: LocalDateTime = DateUtil.now()
): Transaction = Transaction(
    id = id,
    amount = amount.toString(),
    note = note.orEmpty(),
    timestamp = dateTime,
    type = type,
    tagId = null,
    folderId = null,
    excluded = false
)

fun ScheduledTransaction.toEntity(): ScheduledTransactionEntity = ScheduledTransactionEntity(
    id = id,
    amount = amount,
    note = note,
    typeName = type.name,
    repeatModeName = repeatMode.name,
    nextReminderDate = nextReminderDate
)