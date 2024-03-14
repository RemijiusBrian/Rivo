package dev.ridill.rivo.transactionSchedules.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.transactionSchedules.data.local.entity.TxScheduleEntity
import dev.ridill.rivo.transactionSchedules.data.local.relation.ScheduleWithLastTransactionRelation
import java.time.LocalDate

@Dao
interface TxSchedulesDao : BaseDao<TxScheduleEntity> {

    @Query("SELECT * FROM transaction_schedules_table WHERE id = :id")
    suspend fun getScheduleById(id: Long): TxScheduleEntity?

    @Query("UPDATE transaction_schedules_table SET next_reminder_date = :nextDate WHERE id = :id")
    suspend fun updateNextReminderDateForScheduleById(id: Long, nextDate: LocalDate?)

    @Query("SELECT * FROM transaction_schedules_table WHERE next_reminder_date > :date")
    suspend fun getAllSchedulesAfterDate(date: LocalDate): List<TxScheduleEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM transaction_schedules_table schTx
        LEFT OUTER JOIN transaction_table tx ON schTx.id == tx.schedule_id
        WHERE tx.timestamp IS NULL OR tx.timestamp = (SELECT MAX(tx2.timestamp) FROM transaction_table tx2 WHERE tx2.schedule_id = schTx.id)
        ORDER BY CASE
            WHEN schTx.next_reminder_date IS NULL THEN 2
            WHEN strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', schTx.next_reminder_date) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', DATE('now')) THEN 0
            ELSE 1
            END ASC
    """
    )
    fun getAllSchedulesWithLastTransaction(): PagingSource<Int, ScheduleWithLastTransactionRelation>
}