package dev.ridill.rivo.tags.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.rivo.core.data.db.BaseDao
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.tags.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relation.TagAndAggregateRelation
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TagsDao : BaseDao<TagEntity> {
    @Query("SELECT * FROM tag_table WHERE name LIKE '%' || :query || '%' ORDER BY DATETIME(created_timestamp) DESC, name ASC")
    fun getAllTagsPaged(
        query: String
    ): PagingSource<Int, TagEntity>

    @Query(
        """
        SELECT tg.id as id, tg.name as name, tg.color_code as colorCode, tg.is_excluded as excluded, tg.created_timestamp as createdTimestamp, (
            SELECT (
                SELECT IFNULL(SUM(t1.amount), 0.0)
                FROM transaction_table t1
                WHERE t1.type = 'DEBIT'
                AND t1.tag_id = tg.id
                AND (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t1.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
                ) - (
                SELECT IFNULL(SUM(t2.amount), 0.0)
                FROM transaction_table t2
                WHERE t2.type = 'CREDIT'
                AND t2.tag_id = tg.id
                AND (:date IS NULL OR strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', t2.timestamp) = strftime('${UtilConstants.DB_MONTH_AND_YEAR_FORMAT}', :date))
            )
        ) AS aggregate
        FROM tag_table tg
        ORDER BY aggregate DESC, dateTime(tg.created_timestamp) DESC
        LIMIT :limit
    """
    )
    fun getTagAndAggForDateSortedByAggPaged(
        date: LocalDate?,
        limit: Int
    ): PagingSource<Int, TagAndAggregateRelation>

    @Query("SELECT * FROM tag_table WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("SELECT * FROM tag_table WHERE id = :id")
    fun getTagByIdFlow(id: Long): Flow<TagEntity?>

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