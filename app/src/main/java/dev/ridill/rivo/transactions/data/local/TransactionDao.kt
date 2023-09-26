package dev.ridill.rivo.transactions.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.domain.model.TransactionAmountLimits
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Transaction
    @Query(
        """
        SELECT IFNULL(SUM(tx.amount), 0.0)
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        WHERE strftime('%m-%Y', tx.timestamp) = strftime('%m-%Y', :monthAndYear)
        AND (tx.is_excluded = 0 AND IFNULL(tag.is_excluded, 0) = 0)
        AND tx.transaction_direction = 'OUTGOING'
    """
    )
    fun getExpenditureForMonth(monthAndYear: LocalDateTime): Flow<Double>

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
        (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded) THEN 1 ELSE 0 END) AS isExcludedTransaction
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag
        ON tx.tag_id = tag.id
        WHERE strftime('%m-%Y', transactionTimestamp) = strftime('%m-%Y', :monthAndYear)
            AND (:showExcluded = 1 OR isExcludedTransaction = 0)
            AND (:transactionDirectionName IS NULL OR transaction_direction = :transactionDirectionName)
            AND (:tagId IS NULL OR tagId = :tagId)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC, isExcludedTransaction ASC
        """
    )
    fun getTransactionsListForMonth(
        monthAndYear: LocalDateTime,
        transactionDirectionName: String?,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<TransactionDetails>>

    @Query("SELECT DISTINCT(strftime('%Y', timestamp)) AS year FROM transaction_table ORDER BY year DESC")
    fun getYearsFromTransactions(): Flow<List<Int>>

    @Query("UPDATE transaction_table SET is_excluded = :exclude WHERE id IN (:ids)")
    suspend fun toggleExclusionByIds(ids: List<Long>, exclude: Boolean)

    @Query("DELETE FROM transaction_table WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transaction_table WHERE id IN (:ids)")
    suspend fun deleteMultipleTransactionsById(ids: List<Long>)
}