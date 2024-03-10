package dev.ridill.rivo.scheduledTransaction.domain.repository

import dev.ridill.rivo.scheduledTransaction.domain.model.ScheduledTransaction
import java.time.LocalDate

interface ScheduledTransactionRepository {
    suspend fun getTransactionById(id: Long): ScheduledTransaction?
    suspend fun updateNextPaymentDateForTransactionById(id: Long, nextDate: LocalDate)
    suspend fun saveAndScheduleTransaction(transaction: ScheduledTransaction)
    suspend fun cancelTransactionSchedule(transaction: ScheduledTransaction)
    suspend fun rescheduleAllTransactions()
}