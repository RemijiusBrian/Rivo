package dev.ridill.rivo.scheduledTransaction.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.scheduledTransaction.data.local.entity.ScheduledTransactionEntity
import java.time.LocalDate

@Dao
interface ScheduledTransactionDao : BaseDao<ScheduledTransactionEntity> {

    @Query("SELECT * FROM scheduled_transaction_table WHERE id = :id")
    suspend fun getTransactionById(id: Long): ScheduledTransactionEntity?

    @Query("UPDATE scheduled_transaction_table SET next_payment_date = :nextDate WHERE id = :id")
    suspend fun updateNextPaymentDateForTransactionById(id: Long, nextDate: LocalDate)

    @Query("SELECT * FROM scheduled_transaction_table WHERE next_payment_date > :date")
    suspend fun getAllTransactionsAfterDate(date: LocalDate): List<ScheduledTransactionEntity>
}