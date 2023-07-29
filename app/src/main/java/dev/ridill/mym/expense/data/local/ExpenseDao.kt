package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
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

    @Query("SELECT * FROM ExpenseEntity WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM ExpenseEntity WHERE strftime('%m-%Y', datetime) = :monthAndYear ORDER BY dateTime DESC")
    fun getExpensesForMonth(monthAndYear: String): Flow<List<ExpenseEntity>>
}