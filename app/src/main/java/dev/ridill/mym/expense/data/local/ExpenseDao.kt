package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTagRelation
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

    @Transaction
    @Query("SELECT * FROM ExpenseEntity WHERE strftime('%m-%Y', datetime) = :monthAndYear ORDER BY time(dateTime) DESC, id DESC")
    fun getExpensesForMonth(monthAndYear: String): Flow<List<ExpenseWithTagRelation>>

    @Query("SELECT DISTINCT(strftime('%Y', datetime)) as year FROM ExpenseEntity ORDER BY year DESC")
    fun getDistinctYears(): Flow<List<Int>>

    @Transaction
    @Query(
        """
        SELECT *
        FROM ExpenseEntity
        WHERE strftime('%m-%Y', datetime) = :monthAndYear AND (:tagId IS NULL OR tagId = :tagId)
        ORDER BY time(dateTime) DESC, id DESC
    """
    )
    fun getExpenseForMonthByTag(
        monthAndYear: String,
        tagId: String?
    ): Flow<List<ExpenseWithTagRelation>>

    @Query("DELETE FROM ExpenseEntity WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM ExpenseEntity WHERE id IN (:ids)")
    suspend fun deleteMultipleExpenseById(ids: List<Long>)
}