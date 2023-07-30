package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.TagEntity

@Dao
interface TagsDao : BaseDao<TagEntity> {

    @Query("SELECT * FROM TagEntity ORDER BY name ASC")
    fun getTagsList(): List<TagEntity>

    @Query(
        """
        SELECT IFNULL(SUM(exp.amount), 0.0) AS expenditure, tag.name AS tag, tag.colorCode AS color
        FROM ExpenseEntity exp
        LEFT OUTER JOIN TagEntity tag ON exp.tagId == tag.name
        GROUP BY exp.tagId
    """
    )
    fun getTagsWithExpenditures()
}