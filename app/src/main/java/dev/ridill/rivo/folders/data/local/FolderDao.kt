package dev.ridill.rivo.folders.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.relation.FolderAndAggregateAmount
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao : BaseDao<FolderEntity> {

    @Transaction
    @Query(
        """
        SELECT folder.id, folder.name, folder.created_timestamp AS createdTimestamp, folder.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.folder_id = folder.id AND tx1.transaction_type_name = 'DEBIT')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.folder_id = folder.id AND tx2.transaction_type_name = 'CREDIT')
        ) AS aggregateAmount
        FROM folder_table folder
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getFoldersWithAggregateExpenditure(): PagingSource<Int, FolderAndAggregateAmount>

    @Transaction
    @Query(
        """
        SELECT folder.id, folder.name, folder.created_timestamp AS createdTimestamp, folder.is_excluded as excluded,
        ((SELECT IFNULL(SUM(tx1.amount), 0.0) FROM transaction_table tx1 WHERE tx1.folder_id = folder.id AND tx1.transaction_type_name = 'DEBIT')
        - (SELECT IFNULL(SUM(tx2.amount), 0.0) FROM transaction_table tx2 WHERE tx2.folder_id = folder.id AND tx2.transaction_type_name = 'CREDIT')
        ) AS aggregateAmount
        FROM folder_table folder
        WHERE folder.id = :id
        ORDER BY datetime(createdTimestamp) DESC, id DESC
    """
    )
    fun getFolderWithAggregateExpenditureById(id: Long): Flow<FolderAndAggregateAmount?>

    @Query("SELECT * FROM folder_table WHERE name LIKE '%' || :query || '%'")
    fun getFoldersList(query: String): Flow<List<FolderEntity>>

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

    @Query("DELETE FROM folder_table WHERE id = :id")
    suspend fun deleteFolderById(id: Long)

    @Query("DELETE FROM transaction_table WHERE folder_id = :folderId")
    suspend fun deleteTransactionsByFolderId(folderId: Long)

    @Query("UPDATE transaction_table SET folder_id = NULL WHERE folder_id = :folderId")
    suspend fun removeTransactionsFromFolderById(folderId: Long)
}