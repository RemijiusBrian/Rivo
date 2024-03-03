package dev.ridill.rivo.transactions.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
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
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id
        WHERE (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) = 0
        AND tx.type = :typeName
        AND (:dateTime IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', tx.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :dateTime))
    """
    )
    fun getAmountSum(
        typeName: String,
        dateTime: LocalDateTime? = null
    ): Flow<Double>

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
        SELECT * FROM transaction_details_view
        WHERE (:monthAndYear IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :monthAndYear))
            AND (:transactionTypeName IS NULL OR transactionTypeName = :transactionTypeName)
            AND (:tagId IS NULL OR tagId = :tagId)
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR overallExcluded = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC
        """
    )
    fun getTransactionsList(
        monthAndYear: LocalDateTime? = null,
        transactionTypeName: String? = null,
        tagId: Long? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): Flow<List<TransactionDetailsView>>

    @Transaction
    @Query(
        """
        SELECT (
            SELECT IFNULL(SUM(t1.amount), 0.0)
            FROM transaction_table t1
            WHERE (:typeName = 'DEBIT' OR type = 'DEBIT')
            AND (:dateTime IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t1.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :dateTime))
            AND (:addExcluded = 1 OR is_excluded = 0)
            AND (COALESCE(:selectedTxIds, '') = '' OR t1.id IN (:selectedTxIds))
            ) - (
            SELECT IFNULL(SUM(t2.amount), 0.0)
            FROM transaction_table t2
            WHERE (:typeName = 'CREDIT' OR type = 'CREDIT')
            AND (:dateTime IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t2.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :dateTime))
            AND (:addExcluded = 1 OR is_excluded = 0)
            AND (COALESCE(:selectedTxIds, '') = '' OR t2.id IN (:selectedTxIds))
            )
    """
    )
    fun getAmountAggregate(
        dateTime: LocalDateTime?,
        typeName: String? = null,
        addExcluded: Boolean = false,
        selectedTxIds: Set<Long>? = null
    ): Flow<Double>

    @Transaction
    @Query(
        """
        SELECT * FROM transaction_details_view
        WHERE (:monthAndYear IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :monthAndYear))
            AND (:transactionTypeName IS NULL OR transactionTypeName = :transactionTypeName)
            AND (:tagId IS NULL OR tagId = :tagId)
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR overallExcluded = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC
        """
    )
    fun getTransactionsListPaginated(
        monthAndYear: LocalDateTime? = null,
        transactionTypeName: String? = null,
        tagId: Long? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): PagingSource<Int, TransactionDetailsView>

    @Query("SELECT DISTINCT(strftime('${UtilConstants.DB_YEAR_FORMAT}', timestamp)) AS year FROM transaction_table ORDER BY year DESC")
    fun getYearsFromTransactions(): Flow<List<Int>>

    @Query("UPDATE transaction_table SET is_excluded = :exclude WHERE id IN (:ids)")
    suspend fun toggleExclusionByIds(ids: List<Long>, exclude: Boolean)

    @Query("DELETE FROM transaction_table WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)

    @Query("DELETE FROM transaction_table WHERE id IN (:ids)")
    suspend fun deleteMultipleTransactionsById(ids: List<Long>)

    @Query("UPDATE transaction_table SET folder_id = :folderId WHERE id IN (:transactionIds)")
    suspend fun setFolderIdToTransactionsByIds(transactionIds: List<Long>, folderId: Long?)

    @Query("UPDATE transaction_table SET folder_id = NULL WHERE id IN (:ids)")
    suspend fun removeFolderFromTransactionsByIds(ids: List<Long>)
}