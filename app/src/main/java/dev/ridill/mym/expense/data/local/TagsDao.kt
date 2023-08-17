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
        SELECT tag.name as name, tag.colorCode as colorCode, tag.dateCreated as createdTimestamp,
            (SELECT IFNULL(SUM(subExp.amount), 0.0) FROM ExpenseEntity subExp WHERE subExp.tagId = tag.name AND strftime('%m-%Y', subExp.dateTime) = :monthAndYear) as amount
        FROM TagEntity tag
        ORDER BY tag.name ASC 
    """
    )
    fun getTagsWithExpenditureForDate(
        monthAndYear: String
    ): Flow<List<TagWithExpenditureRelation>>

    @Query("UPDATE ExpenseEntity SET tagId = :tag WHERE id IN (:ids)")
    suspend fun assignTagToExpensesWithIds(tag: String, ids: List<Long>)

    @Query("UPDATE ExpenseEntity SET tagId = NULL WHERE id IN (:ids)")
    suspend fun untagExpenses(ids: List<Long>)

    @Query("UPDATE ExpenseEntity SET tagId = NULL WHERE tagId = :name")
    suspend fun untagExpensesByTag(name: String)

    @Query("DELETE FROM TagEntity WHERE name = :name")
    suspend fun deleteTagByName(name: String)

    @Transaction
    suspend fun clearAndDeleteTag(name: String) {
        untagExpensesByTag(name)
        deleteTagByName(name)
    }

    @Query("DELETE FROM ExpenseEntity WHERE tagId = :tag")
    suspend fun deleteExpensesByTag(tag: String)

    @Transaction
    suspend fun deleteTagWithExpenses(tag: String) {
        deleteExpensesByTag(tag)
        deleteTagByName(tag)
    }
}