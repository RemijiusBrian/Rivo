package dev.ridill.rivo.transactions.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.relations.TransactionWithTagRelation
import dev.ridill.rivo.transactions.domain.model.TransactionAmountLimits
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
        SELECT IFNULL(MAX(amount), 0.0) AS upperLimit, IFNULL(MIN(amount), 0.0) AS lowerLimit
        FROM transaction_table
    """
    )
    fun getTransactionAmountRange(): Flow<TransactionAmountLimits>

    @Query("SELECT * FROM transaction_table WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Transaction
    @Query(
        """
        SELECT tx.id AS transactionId,
        tx.note AS transactionNote,
        tx.amount AS transactionAmount,
        tx.timestamp AS transactionTimestamp,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        (CASE WHEN (tx.is_excluded = 1 OR tag.is_excluded = 1) THEN 1 ELSE 0 END) AS isExcludedTransaction
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

    @Query("SELECT DISTINCT(strftime('%Y', timestamp)) AS year FROM transaction_table ORDER BY year DESC")
    fun getYearsFromTransactions(): Flow<List<Int>>

    @Transaction
    @Query(
        """
        SELECT tx.id AS transactionId,
        tx.note AS transactionNote,
        tx.amount AS transactionAmount,
        tx.timestamp AS transactionTimestamp,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        (CASE WHEN (tx.is_excluded = 1 OR tag.is_excluded = 1) THEN 1 ELSE 0 END) AS isExcludedTransaction
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