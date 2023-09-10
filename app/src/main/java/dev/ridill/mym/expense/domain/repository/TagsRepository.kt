package dev.ridill.mym.expense.domain.repository

import androidx.compose.ui.graphics.Color
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getAllTags(): Flow<List<ExpenseTag>>
    fun getTagsWithExpenditures(
        date: LocalDate,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>>

    suspend fun assignTagToExpenses(tagId: Long, ids: List<Long>)
    suspend fun deTagExpenses(ids: List<Long>)
    suspend fun saveTag(
        id: Long,
        name: String,
        color: Color,
        timestamp: LocalDateTime = DateUtil.now()
    ): Long

    suspend fun deleteTagById(id: Long)
    suspend fun deleteTagWithExpenses(tagId: Long)
}