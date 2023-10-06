package dev.ridill.rivo.transactions.data.local

import androidx.paging.PagingSource
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
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id
        WHERE strftime('%m-%Y', tx.timestamp) = strftime('%m-%Y', :monthAndYear)
        AND (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) = 0
        AND tx.type = 'DEBIT'
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
        tx.type AS transactionTypeName,
        tx.is_excluded AS isTransactionExcluded,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        tag.is_excluded AS isTagExcluded,
        folder.id AS folderId,
        folder.name AS folderName,
        folder.created_timestamp AS folderCreatedTimestamp,
        folder.is_excluded AS isFolderExcluded
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id
        WHERE (:monthAndYear IS NULL OR strftime('%m-%Y', transactionTimestamp) = strftime('%m-%Y', :monthAndYear))
            AND (:transactionTypeName IS NULL OR type = :transactionTypeName)
            AND (:tagId IS NULL OR tagId = :tagId)
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC
        """
    )
    fun getTransactionsList(
        monthAndYear: LocalDateTime? = null,
        transactionTypeName: String? = null,
        tagId: Long? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): Flow<List<TransactionDetails>>

    @Transaction
    @Query(
        """
        SELECT tx.id AS transactionId,
        tx.note AS transactionNote,
        tx.amount AS transactionAmount,
        tx.timestamp AS transactionTimestamp,
        tx.type AS transactionTypeName,
        tx.is_excluded AS isTransactionExcluded,
        tag.id AS tagId,
        tag.name AS tagName,
        tag.color_code AS tagColorCode,
        tag.created_timestamp AS tagCreatedTimestamp,
        tag.is_excluded AS isTagExcluded,
        folder.id AS folderId,
        folder.name AS folderName,
        folder.created_timestamp AS folderCreatedTimestamp,
        folder.is_excluded AS isFolderExcluded
        FROM transaction_table tx
        LEFT OUTER JOIN tag_table tag ON tx.tag_id = tag.id
        LEFT OUTER JOIN folder_table folder ON tx.folder_id = folder.id
        WHERE (:monthAndYear IS NULL OR strftime('%m-%Y', transactionTimestamp) = strftime('%m-%Y', :monthAndYear))
            AND (:transactionTypeName IS NULL OR type = :transactionTypeName)
            AND (:tagId IS NULL OR tagId = :tagId)
            AND (:folderId IS NULL OR folderId = :folderId)
            AND (:showExcluded = 1 OR (CASE WHEN 1 IN (tx.is_excluded, tag.is_excluded, folder.is_excluded) THEN 1 ELSE 0 END) = 0)
        ORDER BY datetime(transactionTimestamp) DESC, transactionId DESC
        """
    )
    fun getTransactionsListPaginated(
        monthAndYear: LocalDateTime? = null,
        transactionTypeName: String? = null,
        tagId: Long? = null,
        folderId: Long? = null,
        showExcluded: Boolean = true
    ): PagingSource<Int, TransactionDetails>

    @Query("SELECT DISTINCT(strftime('%Y', timestamp)) AS year FROM transaction_table ORDER BY year DESC")
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