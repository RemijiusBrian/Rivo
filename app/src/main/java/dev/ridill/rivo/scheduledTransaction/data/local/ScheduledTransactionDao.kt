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

    @Query("UPDATE scheduled_transaction_table SET next_reminder_date = :nextDate WHERE id = :id")
    suspend fun updateNextReminderDateForTransactionById(id: Long, nextDate: LocalDate?)

    @Query("SELECT * FROM scheduled_transaction_table WHERE next_reminder_date > :date")
    suspend fun getAllTransactionsAfterDate(date: LocalDate): List<ScheduledTransactionEntity>

    /*  @Query(
          """
          SELECT schTx.id as id, tx.id as txId
          FROM scheduled_transaction_table schTx
          JOIN transaction_table tx ON tx.schedule_id = schTx.id
              AND strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', schTx.next_payment_date) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', tx.timestamp)
      """
      )
      fun getScheduledTransactions(date: LocalDate): Flow<List<ScheduledTransactionEntity>>*/
}