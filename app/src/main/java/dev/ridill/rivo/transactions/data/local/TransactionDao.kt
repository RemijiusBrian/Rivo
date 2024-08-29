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
import java.time.LocalDate

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
        AND (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', tx.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
    """
    )
    fun getAmountSum(
        typeName: String,
        date: LocalDate? = null
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
        WHERE (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
            AND (:transactionTypeName IS NULL OR transactionTypeName = :transactionTypeName)
            AND (COALESCE(:tagIds, 0) = 0 OR tagId IN (:tagIds))
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR isTagExcluded = 1 OR overallExcluded = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionNote DESC, tagName DESC, folderName DESC
        """
    )
    fun getTransactionsList(
        date: LocalDate? = null,
        transactionTypeName: String? = null,
        tagIds: Set<Long>? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): Flow<List<TransactionDetailsView>>

    @Transaction
    @Query(
        """
        SELECT (
            SELECT IFNULL(SUM(t1.transactionAmount), 0.0)
            FROM transaction_details_view t1
            WHERE (:typeName = 'DEBIT' OR t1.transactionTypeName = 'DEBIT')
            AND (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t1.transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
            AND (COALESCE(:tagIds, 0) = 0 OR t1.tagId IN (:tagIds))
            AND (:addExcluded = 1 OR t1.overallExcluded = 0)
            AND (COALESCE(:selectedTxIds, 1) = 1 OR t1.transactionId IN (:selectedTxIds))
            ) - (
            SELECT IFNULL(SUM(t2.transactionAmount), 0.0)
            FROM transaction_details_view t2
            WHERE (:typeName = 'CREDIT' OR t2.transactionTypeName = 'CREDIT')
            AND (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t2.transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
            AND (COALESCE(:tagIds, 0) = 0 OR t2.tagId IN (:tagIds))
            AND (:addExcluded = 1 OR t2.overallExcluded = 0)
            AND (COALESCE(:selectedTxIds, 1) = 1 OR t2.transactionId IN (:selectedTxIds))
        )
    """
    )
    fun getAmountAggregate(
        date: LocalDate? = null,
        typeName: String? = null,
        tagIds: Set<Long>? = null,
        addExcluded: Boolean = false,
        selectedTxIds: Set<Long>? = null
    ): Flow<Double>

    @Query(
        """
        SELECT (
            SELECT IFNULL(SUM(t1.amount), 0.0)
            FROM transaction_table t1
            WHERE t1.type = 'DEBIT'
            AND t1.id IN (:ids)
            ) - (
            SELECT IFNULL(SUM(t2.amount), 0.0)
            FROM transaction_table t2
            WHERE t2.type = 'CREDIT'
            AND t2.id IN (:ids)
        )
    """
    )
    suspend fun getAggregateAmountByIds(ids: Set<Long>): Double

    @Transaction
    @Query(
        """
        SELECT * FROM transaction_details_view
        WHERE (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', transactionTimestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
            AND (:transactionTypeName IS NULL OR transactionTypeName = :transactionTypeName)
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR overallExcluded = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC
        """
    )
    fun getTransactionsListPaginated(
        date: LocalDate? = null,
        transactionTypeName: String? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): PagingSource<Int, TransactionDetailsView>

    @Query("SELECT DISTINCT(strftime('${UtilConstants.DB_YEAR_FORMAT}', timestamp)) AS year FROM transaction_table ORDER BY year DESC")
    fun getYearsFromTransactions(): Flow<List<Int>>

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