package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.local.relations.TagWithExpenditureRelation
import kotlinx.coroutines.flow.Flow

@Dao
interface TagsDao : BaseDao<TagEntity> {

    @Query("SELECT * FROM TagEntity ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Transaction
    @Query(
        """
        SELECT tag.name as name, tag.colorCode as colorCode, tag.dateCreated as createdTimestamp, IFNULL(SUM(exp.amount), 0.0) as amount
        FROM TagEntity tag
        JOIN ExpenseEntity exp
        ON tag.name = exp.tagId
        GROUP BY tag.name
        HAVING strftime('%m-%Y', exp.dateTime) = :monthAndYear
    """
    )
    fun getTagsWithExpenditureForDate(
        monthAndYear: String
    ): Flow<List<TagWithExpenditureRelation>>

    @Query("UPDATE ExpenseEntity SET tagId = :tag WHERE id IN (:ids)")
    suspend fun assignTagToExpensesWithIds(tag: String, ids: List<Long>)

    @Query("UPDATE ExpenseEntity SET tagId = NULL WHERE id IN (:ids)")
    suspend fun untagExpenses(ids: List<Long>)
}