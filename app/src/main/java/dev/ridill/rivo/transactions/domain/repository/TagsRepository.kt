package dev.ridill.rivo.transactions.domain.repository

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.transactions.domain.model.ExpenseTag
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure
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
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long

    suspend fun deleteTagById(id: Long)
    suspend fun deleteTagWithExpenses(tagId: Long)
}