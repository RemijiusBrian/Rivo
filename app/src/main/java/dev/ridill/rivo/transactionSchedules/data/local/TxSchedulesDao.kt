package dev.ridill.rivo.transactionSchedules.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.transactionSchedules.data.local.entity.TxScheduleEntity
import dev.ridill.rivo.transactionSchedules.data.local.relation.ScheduleWithLastPaidDateRelation
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
        SELECT schTx.id as id,
        schTx.amount as amount,
        schTx.note as note,
        schTx.next_reminder_date as nextReminderDate,
        (SELECT tx.timestamp
            FROM transaction_table tx
            WHERE tx.schedule_id = schTx.id
            ORDER BY tx.timestamp DESC
            LIMIT 1
        ) as lastPaymentTimestamp
        FROM transaction_schedules_table schTx
        ORDER BY CASE
                WHEN (nextReminderDate IS NULL
                AND strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', lastPaymentTimestamp) <= strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', DATE('now'))
                ) THEN 1
                ELSE 0
            END ASC,
            nextReminderDate ASC,
            id DESC
    """
    )
    fun getAllSchedulesWithLastPaidDates(): PagingSource<Int, ScheduleWithLastPaidDateRelation>
}