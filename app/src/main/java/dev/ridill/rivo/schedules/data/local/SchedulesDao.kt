package dev.ridill.rivo.schedules.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.schedules.data.local.entity.ScheduleEntity
import dev.ridill.rivo.schedules.data.local.relation.ScheduleWithLastTransactionRelation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SchedulesDao : BaseDao<ScheduleEntity> {

    @Query("SELECT * FROM schedules_table WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Query("UPDATE schedules_table SET next_reminder_date = :nextDate WHERE id = :id")
    suspend fun updateNextReminderDateForScheduleById(id: Long, nextDate: LocalDate?)

    @Query("SELECT * FROM schedules_table WHERE next_reminder_date > :date")
    suspend fun getAllSchedulesAfterDate(date: LocalDate): List<ScheduleEntity>

    @Transaction
    @Query(
        """
        SELECT *
        FROM schedules_table schTx
        LEFT OUTER JOIN transaction_table tx ON schTx.id = tx.schedule_id
        WHERE tx.timestamp = (SELECT MAX(tx2.timestamp) FROM transaction_table tx2 WHERE tx2.schedule_id = schTx.id) OR tx.timestamp IS NULL
        ORDER BY CASE
            WHEN schTx.next_reminder_date IS NULL THEN 2
            WHEN strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', schTx.next_reminder_date) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', DATE('now')) THEN 0
            ELSE 1
            END ASC
    """
    )
    @RewriteQueriesToDropUnusedColumns
    fun getAllSchedulesWithLastTransactionPaged(): PagingSource<Int, ScheduleWithLastTransactionRelation>

    @Transaction
    @Query(
        """
        SELECT *
        FROM schedules_table
        WHERE strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', next_reminder_date) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date)
        AND DATE(next_reminder_date) >= DATE('now')
        ORDER BY DATE(next_reminder_date) ASC
    """
    )
    fun getUpcomingSchedulesForDate(date: LocalDate): Flow<List<ScheduleEntity>>

    @Query("DELETE FROM schedules_table WHERE id IN (:ids)")
    suspend fun deleteSchedulesById(ids: Set<Long>)
}