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

    suspend fun assignTagToExpenses(tagName: String, ids: List<Long>)
    suspend fun untagExpenses(ids: List<Long>)
    suspend fun saveTag(name: String, color: Color, timestamp: LocalDateTime = DateUtil.now())
    suspend fun deleteTagByName(name: String)
    suspend fun deleteTagWithExpenses(tag: String)
}