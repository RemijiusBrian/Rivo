package dev.ridill.rivo.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.settings.data.local.entity.BudgetPreferenceEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BudgetPreferenceDao : BaseDao<BudgetPreferenceEntity> {
    @Query(
        """
        SELECT IFNULL(amount, 0)
        FROM budget_preference_table
        WHERE strftime(date, '%Y-%M') = strftime(:date, '%Y-%M') OR
            date = (SELECT MAX(date) FROM budget_preference_table WHERE date <= :date)
        LIMIT 1
    """
    )
    fun getBudgetAmountForDateOrNext(date: LocalDate): Flow<Long>
}