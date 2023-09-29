package dev.ridill.rivo.transactionFolders.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactionFolders.data.local.entity.TransactionFolderEntity
import dev.ridill.rivo.transactionFolders.data.local.relation.FolderAndAggregateAmount
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionFolderDao : BaseDao<TransactionFolderEntity> {

    @Transaction
    @Query(
        """
        SELECT txFolder.id, txFolder.name, txFolder.created_timestamp AS createdTimestamp, txFolder.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.folder_id = txFolder.id AND tx1.transaction_type_name = 'DEBIT')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.folder_id = txFolder.id AND tx2.transaction_type_name = 'CREDIT')
        ) AS aggregateAmount
        FROM transaction_folder_table txFolder
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getFoldersWithAggregateExpenditure(): Flow<List<FolderAndAggregateAmount>>

    @Transaction
    @Query(
        """
        SELECT txFolder.id, txFolder.name, txFolder.created_timestamp AS createdTimestamp, txFolder.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.folder_id = txFolder.id AND tx1.transaction_type_name = 'DEBIT')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.folder_id = txFolder.id AND tx2.transaction_type_name = 'CREDIT')
        ) AS aggregateAmount
        FROM transaction_folder_table txFolder
        WHERE txFolder.id = :id
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getFolderWithAggregateExpenditureById(id: Long): Flow<FolderAndAggregateAmount?>

    @Query("SELECT * FROM transaction_folder_table WHERE name LIKE '%' || :query || '%'")
    fun getFoldersList(query: String): Flow<List<TransactionFolderEntity>>

    @Transaction
    suspend fun deleteFolderOnlyById(id: Long) {
        removeTransactionsFromFolderById(id)
        deleteFolderById(id)
    }

    @Transaction
    suspend fun deleteFolderAndTransactionsById(id: Long) {
        deleteTransactionsByFolderId(id)
        deleteFolderById(id)
    }

    @Query("DELETE FROM transaction_folder_table WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("DELETE FROM transaction_table WHERE folder_id = :folderId")
    suspend fun deleteTransactionsByFolderId(folderId: Long)

    @Query("UPDATE transaction_table SET folder_id = NULL WHERE folder_id = :folderId")
    suspend fun removeTransactionsFromFolderById(folderId: Long)
}