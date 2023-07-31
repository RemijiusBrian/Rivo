package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTag
import dev.ridill.mym.expense.domain.model.ExpenseLimits
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao : BaseDao<ExpenseEntity> {

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0.0)
        FROM ExpenseEntity
        WHERE strftime('%m-%Y', datetime) = :monthAndYear
    """
    )
    fun getExpenditureForMonth(monthAndYear: String): Flow<Double>

    @Query(
        """
        SELECT IFNULL(MAX(amount), 0.0) as upperLimit, IFNULL(MIN(amount), 0.0) as lowerLimit
        FROM ExpenseEntity
    """
    )
    fun getExpenseRange(): Flow<ExpenseLimits>

    @Query("SELECT * FROM ExpenseEntity WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM ExpenseEntity WHERE strftime('%m-%Y', datetime) = :monthAndYear ORDER BY date(dateTime) DESC")
    fun getExpensesForMonth(monthAndYear: String): Flow<List<ExpenseWithTag>>

    @Query("DELETE FROM ExpenseEntity WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)
}