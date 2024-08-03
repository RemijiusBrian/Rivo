package dev.ridill.rivo.folders.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.folders.data.local.entity.FolderEntity
import dev.ridill.rivo.folders.data.local.views.FolderAndAggregateAmountView
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao : BaseDao<FolderEntity> {
    @Transaction
    @Query(
        """SELECT * FROM folder_and_aggregate_amount_view
        ORDER BY CASE
                WHEN aggregateAmount > 0.0 THEN 1
                WHEN aggregateAmount < 0.0 THEN 0
                WHEN aggregateAmount = 0.0 THEN -1
            END DESC,
            datetime(createdTimestamp) DESC
        """
    )
    fun getFoldersWithAggregateExpenditure(): PagingSource<Int, FolderAndAggregateAmountView>

    @Transaction
    @Query("SELECT * FROM folder_and_aggregate_amount_view WHERE id = :id ORDER BY datetime(createdTimestamp) DESC, id DESC")
    fun getFolderWithAggregateExpenditureById(id: Long): Flow<FolderAndAggregateAmountView?>

    @Query("SELECT * FROM folder_table WHERE name LIKE '%' || :query || '%'")
    fun getFoldersList(query: String): PagingSource<Int, FolderEntity>

    @Query("SELECT * FROM folder_table WHERE id = :id")
    suspend fun getFolderById(id: Long): FolderEntity?

    @Query("SELECT * FROM folder_table WHERE id = :id")
    fun getFolderByIdFlow(id: Long): Flow<FolderEntity?>

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