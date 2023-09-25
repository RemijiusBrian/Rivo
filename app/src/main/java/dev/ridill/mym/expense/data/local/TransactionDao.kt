package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.TransactionEntity
import dev.ridill.mym.expense.data.local.relations.TransactionWithTagRelation
import dev.ridill.mym.expense.domain.model.TransactionAmountLimits
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query(
        """
        SELECT IFNULL(SUM(amount), 0.0)
        FROM transaction_table
        WHERE strftime('%m-%Y', timestamp) = :monthAndYear AND is_excluded = 0
    """
    )
    fun getExpenditureForMonth(monthAndYear: String): Flow<Double>

    @Query(
        """
        SELECT IFNULL(MAX(amount), 0.0) as upperLimit, IFNULL(MIN(amount), 0.0) as lowerLimit
        FROM transaction_table
    """
    )
    fun getTransactionAmountRange(): Flow<TransactionAmountLimits>

    @Query("SELECT * FROM transaction_table WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Transaction
    @Query(
        """
        SELECT tx.id as transactionId,
        tx.note as transactionNote,
        tx.amount as transactionAmount,
        tx.timestamp as transactionTimestamp,
        tag.id as tagId,
        tag.name as tagName,
        tag.color_code as tagColorCode,
        tag.created_timestamp as tagCreatedTimestamp,
        (tx.is_excluded = 1 OR tag.is_excluded = 1) as isExcludedTransaction
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        WHERE strftime('%m-%Y', transactionTimestamp) = :monthAndYear
            AND (:showExcluded = 1 OR isExcludedTransaction = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC, isExcludedTransaction ASC
        """
    )
    fun getTransactionsForMonth(
        monthAndYear: String,
        showExcluded: Boolean
    ): Flow<List<TransactionWithTagRelation>>

    @Query("SELECT DISTINCT(strftime('%Y', timestamp)) as year FROM transaction_table ORDER BY year DESC")
    fun getYearsFromTransactions(): Flow<List<Int>>

    @Transaction
    @Query(
        """
        SELECT tx.id as transactionId,
        tx.note as transactionNote,
        tx.amount as transactionAmount,
        tx.timestamp as transactionTimestamp,
        tag.id as tagId,
        tag.name as tagName,
        tag.color_code as tagColorCode,
        tag.created_timestamp as tagCreatedTimestamp,
        (tx.is_excluded = 1 OR tag.is_excluded = 1) as isExcludedTransaction
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        WHERE strftime('%m-%Y', transactionTimestamp) = :monthAndYear
            AND (:tagId IS NULL OR tagId = :tagId)
            AND (:showExcluded = 1 OR isExcludedTransaction = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC, isExcludedTransaction ASC
    """
    )
    fun getTransactionForMonthByTag(
        monthAndYear: String,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<TransactionWithTagRelation>>

    @Query("UPDATE transaction_table SET is_excluded = :exclude WHERE id IN (:ids)")
    suspend fun toggleExclusionByIds(ids: List<Long>, exclude: Boolean)

    @Query("DELETE FROM transaction_table WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transaction_table WHERE id IN (:ids)")
    suspend fun deleteMultipleTransactionsById(ids: List<Long>)
}