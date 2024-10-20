package dev.ridill.rivo.transactions.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.domain.model.TransactionAmountLimits
import dev.ridill.rivo.transactions.domain.model.TransactionDateLimits
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao : BaseDao<TransactionEntity> {

    @Query(
        """
        SELECT IFNULL(MAX(amount), 0.0) AS upperLimit, IFNULL(MIN(amount), 0.0) AS lowerLimit
        FROM transaction_table
    """
    )
    fun getTransactionAmountRange(): Flow<TransactionAmountLimits>

    @Query(
        """
        SELECT IFNULL(MAX(DATE(timestamp)), DATE('now')) AS maxDate, IFNULL(MIN(DATE(timestamp)), DATE('now')) AS minDate
        FROM transaction_table
    """
    )
    fun getDateLimits(): Flow<TransactionDateLimits>

    @Query("SELECT * FROM transaction_table WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Transaction
    @Query(
        """
        SELECT * FROM transaction_details_view
        WHERE ((:startDate IS NULL OR :endDate IS NULL) OR DATE(transactionTimestamp) BETWEEN DATE(:startDate) AND DATE(:endDate))
            AND (:type IS NULL OR transactionType = :type)
            AND (COALESCE(:tagIds, 0) = 0 OR tagId IN (:tagIds))
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR overallExcluded = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionNote DESC, tagName DESC, folderName DESC
        """
    )
    fun getTransactionsPaged(
        startDate: LocalDate?,
        endDate: LocalDate?,
        type: TransactionType?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?
    ): PagingSource<Int, TransactionDetailsView>

    @Query(
        """
        SELECT IFNULL(SUM(
            CASE
                WHEN transactionType = 'DEBIT' THEN transactionAmount
                WHEN transactionType = 'CREDIT' THEN -transactionAmount
            END
        ), 0)
        FROM transaction_details_view
        WHERE (:type IS NULL OR transactionType = :type)
            AND ((:startDate IS NULL OR :endDate IS NULL) OR DATE(transactionTimestamp) BETWEEN DATE(:startDate) AND DATE(:endDate))
            AND (COALESCE(:tagIds, 0) = 0 OR tagId IN (:tagIds))
            AND (:addExcluded = 1 OR overallExcluded = 0)
            AND (COALESCE(:selectedTxIds, 0) = 0 OR transactionId IN (:selectedTxIds))
    """
    )
    fun getAmountAggregate(
        startDate: LocalDate?,
        endDate: LocalDate?,
        type: TransactionType?,
        tagIds: Set<Long>?,
        addExcluded: Boolean,
        selectedTxIds: Set<Long>?
    ): Flow<Double>

    @Query(
        """
        SELECT IFNULL(SUM(
            CASE
                WHEN type = 'DEBIT' THEN amount
                WHEN type = 'CREDIT' THEN -amount
            END
        ), 0)
        FROM transaction_table
        WHERE id in (:ids)
    """
    )
    suspend fun getAggregateAmountByIds(ids: Set<Long>): Double

    @Query("UPDATE transaction_table SET tag_id = :tagId WHERE id IN (:ids)")
    suspend fun setTagIdToTransactionsByIds(tagId: Long?, ids: Set<Long>)

    @Query("UPDATE transaction_table SET is_excluded = :exclude WHERE id IN (:ids)")
    suspend fun toggleExclusionByIds(ids: Set<Long>, exclude: Boolean)

    @Query("DELETE FROM transaction_table WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM transaction_table WHERE id IN (:ids)")
    suspend fun deleteMultipleTransactionsById(ids: Set<Long>)

    @Query("UPDATE transaction_table SET folder_id = :folderId WHERE id IN (:ids)")
    suspend fun setFolderIdToTransactionsByIds(ids: Set<Long>, folderId: Long?)

    @Query("UPDATE transaction_table SET folder_id = NULL WHERE id IN (:ids)")
    suspend fun removeFolderFromTransactionsByIds(ids: Set<Long>)
}