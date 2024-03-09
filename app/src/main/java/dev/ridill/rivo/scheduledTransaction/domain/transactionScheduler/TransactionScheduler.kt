package dev.ridill.rivo.scheduledTransaction.domain.transactionScheduler

import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction

interface TransactionScheduler {
    fun schedule(transaction: ScheduledTransaction)
    fun cancel(transaction: ScheduledTransaction)

    companion object {
        const val TX_ID = "TX_ID"
    }
}