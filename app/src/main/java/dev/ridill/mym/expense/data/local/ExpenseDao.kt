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
        WHERE strftime('%m-%Y', timestamp) = :monthAndYear AND isExcluded = 0
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
    @Query(
        """
        SELECT exp.id as expenseId,
        exp.note as expenseNote,
        exp.amount as expenseAmount,
        exp.timestamp as expenseTimestamp,
        tag.id as tagId,
        tag.name as tagName,
        tag.colorCode as tagColorCode,
        tag.createdTimestamp as tagCreatedTimestamp,
        (exp.isExcluded OR tag.isExcluded) as isExcludedTransaction
        FROM ExpenseEntity exp
        LEFT OUTER JOIN TagEntity tag
        ON exp.tagId = tag.id
        WHERE strftime('%m-%Y', expenseTimestamp) = :monthAndYear AND (:showExcluded = 1 OR isExcludedTransaction)
        ORDER BY datetime(expenseTimestamp) DESC, expenseId DESC, isExcludedTransaction ASC
        """
    )
    fun getExpensesForMonth(
        monthAndYear: String,
        showExcluded: Boolean
    ): Flow<List<ExpenseWithTagRelation>>

    @Query("SELECT DISTINCT(strftime('%Y', timestamp)) as year FROM ExpenseEntity ORDER BY year DESC")
    fun getDistinctYears(): Flow<List<Int>>

    @Transaction
    @Query(
        """
        SELECT exp.id as expenseId,
        exp.note as expenseNote,
        exp.amount as expenseAmount,
        exp.timestamp as expenseTimestamp,
        tag.id as tagId,
        tag.name as tagName,
        tag.colorCode as tagColorCode,
        tag.createdTimestamp as tagCreatedTimestamp,
        (exp.isExcluded OR tag.isExcluded) as isExcludedTransaction
        FROM ExpenseEntity exp
        LEFT OUTER JOIN TagEntity tag
        ON exp.tagId = tag.id
        WHERE strftime('%m-%Y', expenseTimestamp) = :monthAndYear AND (:tagId IS NULL OR tagId = :tagId) AND (:showExcluded = 1 OR isExcludedTransaction)
        ORDER BY datetime(expenseTimestamp) DESC, expenseId DESC, isExcludedTransaction ASC
    """
    )
    fun getExpenseForMonthByTag(
        monthAndYear: String,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<ExpenseWithTagRelation>>

    @Query("UPDATE ExpenseEntity SET isExcluded = :exclude WHERE id IN (:ids)")
    suspend fun toggleExclusionByIds(ids: List<Long>, exclude: Boolean)

    @Query("DELETE FROM ExpenseEntity WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("DELETE FROM ExpenseEntity WHERE id IN (:ids)")
    suspend fun deleteMultipleExpenseById(ids: List<Long>)
}