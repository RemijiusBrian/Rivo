package dev.ridill.mym.expense.data.local

import androidx.room.Dao
import androidx.room.Query
import dev.ridill.mym.core.data.db.BaseDao
import dev.ridill.mym.expense.data.local.entity.TagEntity

@Dao
interface TagsDao : BaseDao<TagEntity> {

    @Query("""
        SELECT tagId as tag, SUM()
        FROM TagEntity te
        JOIN ExpenseEntity 
    """)
    fun getTagsWithExpenditure()
}