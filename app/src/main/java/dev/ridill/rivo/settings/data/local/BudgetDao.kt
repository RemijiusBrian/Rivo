package dev.ridill.rivo.settings.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.settings.data.local.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BudgetDao : BaseDao<BudgetEntity> {

    @Query(
        """
        SELECT IFNULL(amount, 0)
        FROM budget_table
        WHERE strftime(date, '%Y-%M') = strftime(:date, '%Y-%M') OR date = (SELECT MAX(date) FROM budget_table)
        LIMIT 1
    """
    )
    fun getBudgetAmountForDateOrLatest(date: LocalDate): Flow<Long>
}