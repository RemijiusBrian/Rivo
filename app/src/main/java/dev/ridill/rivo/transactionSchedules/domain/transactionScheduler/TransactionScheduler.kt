package dev.ridill.rivo.transactionSchedules.domain.transactionScheduler

import dev.ridill.rivo.transactionSchedules.domain.model.TxSchedule

interface TransactionScheduler {
    fun schedule(transaction: TxSchedule)
    fun cancel(transaction: TxSchedule)

    companion object {
        const val TX_ID = "TX_ID"
    }
}