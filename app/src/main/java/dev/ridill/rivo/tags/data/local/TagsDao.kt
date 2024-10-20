package dev.ridill.rivo.tags.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TagsDao : BaseDao<TagEntity> {
    @Query(
        """
        SELECT *
        FROM tag_table
        WHERE (name LIKE '%' || :query || '%')
        ORDER BY DATETIME(created_timestamp) DESC, name ASC
        LIMIT :limit
    """
    )
    fun getAllTagsPaged(
        query: String,
        limit: Int
    ): PagingSource<Int, TagEntity>

    @Transaction
    @Query(
        """
        SELECT tg.id as id, tg.name as name,
            tg.color_code as colorCode,
            tg.is_excluded as excluded,
            tg.created_timestamp as createdTimestamp,
            IFNULL(SUM(
                CASE
                    WHEN tx.type = 'DEBIT' THEN tx.amount
                    WHEN tx.type = 'CREDIT' THEN -tx.amount
                END
                ), 0) as aggregate
        FROM tag_table tg
        JOIN transaction_table tx ON tg.id = tx.tag_id
        WHERE tx.is_excluded = 0
        GROUP BY tg.id
        HAVING ((:startDate IS NULL OR :endDate IS NULL) OR DATE(tx.timestamp) BETWEEN DATE(:startDate) AND DATE(:endDate))
        ORDER BY aggregate DESC, DATETIME(tg.created_timestamp) DESC, tg.name ASC
        LIMIT :limit
    """
    )
    fun getTagAndAggregatePaged(
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Int
    ): PagingSource<Int, TagAndAggregateRelation>

    @Query("SELECT * FROM tag_table WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tag_table WHERE id IN (:ids)")
    fun getTagsByIdFlow(ids: Set<Long>): Flow<List<TagEntity>>

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