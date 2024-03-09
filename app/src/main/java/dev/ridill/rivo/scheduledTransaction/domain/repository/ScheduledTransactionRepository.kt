package dev.ridill.rivo.scheduledTransaction.domain.repository

import dev.ridill.rivo.scheduledTransaction.data.local.entity.ScheduledTransactionEntity
import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import org.jetbrains.annotations.Async.Schedule
import java.time.LocalDate

interface ScheduledTransactionRepository {
    suspend fun getTransactionById(id: Long): ScheduledTransaction?
    suspend fun updateNextPaymentDateForTransactionById(id: Long, nextDate: LocalDate)
    suspend fun scheduleTransaction(transaction: ScheduledTransaction)
    suspend fun cancelTransactionSchedule(transaction: ScheduledTransaction)
    suspend fun rescheduleAllTransactions()
}