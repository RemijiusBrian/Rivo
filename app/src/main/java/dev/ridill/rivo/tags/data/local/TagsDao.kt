package dev.ridill.rivo.tags.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDao : BaseDao<TagEntity> {
    @Query("SELECT * FROM tag_table ORDER BY DATETIME(created_timestamp) DESC, name ASC")
    fun getAllTagsPaged(): PagingSource<Int, TagEntity>

    @Query("SELECT * FROM tag_table WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tag_table WHERE id = :id")
    fun getTagByIdFlow(id: Long): Flow<TagEntity?>

    @Query("UPDATE transaction_table SET tag_id = :tagId WHERE id IN (:ids)")
    suspend fun assignTagToTransactionsByIds(tagId: Long, ids: List<Long>)

    @Query("UPDATE transaction_table SET tag_id = NULL WHERE id IN (:ids)")
    suspend fun untagTransactionsByIds(ids: List<Long>)

    @Transaction
    suspend fun untagTransactionsAndDeleteTag(id: Long) {
        untagTransactionsByTag(id)
        deleteTagById(id)
    }

    @Transaction
    suspend fun deleteTagWithTransactions(id: Long) {
        deleteTransactionsByTag(id)
        deleteTagById(id)
    }

    @Query("UPDATE transaction_table SET tag_id = NULL WHERE tag_id = :id")
    suspend fun untagTransactionsByTag(id: Long)

    @Query("DELETE FROM tag_table WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    @Query("DELETE FROM transaction_table WHERE tag_id = :id")
    suspend fun deleteTransactionsByTag(id: Long)
}