package dev.ridill.rivo.transactions.domain.repository

import androidx.compose.ui.graphics.Color
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TagInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getAllTags(): Flow<List<Tag>>
    fun getTagsWithExpenditures(date: LocalDate): Flow<List<TagInfo>>

    suspend fun assignTagToTransactions(tagId: Long, ids: List<Long>)
    suspend fun untagTransactions(ids: List<Long>)
    suspend fun saveTag(
        id: Long,
        name: String,
        color: Color,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long

    suspend fun deleteTagById(id: Long)
    suspend fun deleteTagWithTransactions(tagId: Long)
    suspend fun getTagById(id: Long): Tag?
}