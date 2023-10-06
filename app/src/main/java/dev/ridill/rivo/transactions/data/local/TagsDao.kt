package dev.ridill.rivo.transactions.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relations.TagWithExpenditureRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDao : BaseDao<TagEntity> {

    @Query("SELECT * FROM tag_table ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Transaction
    @Query(
        """
        SELECT tag.id as id,
        tag.name as name,
        tag.color_code as colorCode,
        tag.created_timestamp as createdTimestamp,
        tag.is_excluded as isExcluded,
        (SELECT IFNULL(SUM(subTx.amount), 0.0)
            FROM transaction_table subTx
            WHERE subTx.tag_id = tag.id
            AND strftime('%m-%Y', subTx.timestamp) = :monthAndYear
            AND subTx.type = 'DEBIT'   
        ) as amount
        FROM tag_table tag
        ORDER BY name ASC, datetime(createdTimestamp) DESC
    """
    )
    fun getTagsWithExpenditureForDate(
        monthAndYear: String
    ): Flow<List<TagWithExpenditureRelation>>

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