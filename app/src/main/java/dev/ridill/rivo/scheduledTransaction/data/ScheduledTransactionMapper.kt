package dev.ridill.rivo.scheduledTransaction.data

import dev.ridill.rivo.scheduledTransaction.data.local.entity.ScheduledTransactionEntity
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import dev.ridill.rivo.scheduledTransaction.domain.model.TransactionRepeatMode

fun ScheduledTransactionEntity.toScheduledTransaction(): ScheduledTransaction =
    ScheduledTransaction(
        id = id,
        repeatMode = TransactionRepeatMode.valueOf(repeatModeName),
        nextPaymentDate = nextPaymentDate,
        amount = amount
    )