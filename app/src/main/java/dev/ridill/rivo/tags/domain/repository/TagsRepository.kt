package dev.ridill.rivo.tags.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagSelector
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getTagSelectorsPagingData(): Flow<PagingData<TagSelector>>
    fun getTagsPagingData(): Flow<PagingData<Tag>>
    fun getTopTags(
        date: LocalDate?,
        limit: Int = DEFAULT_TOP_TAG_LIMIT
    ): Flow<PagingData<Tag>>

    suspend fun assignTagToTransactions(tagId: Long, ids: List<Long>)
    suspend fun untagTransactions(ids: List<Long>)
    suspend fun saveTag(
        id: Long,
        name: String,
        colorCode: Int,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long

    suspend fun deleteTagById(id: Long)
    suspend fun deleteTagWithTransactions(tagId: Long)
    suspend fun getTagById(id: Long): Tag?
    fun getTagByIdFlow(id: Long): Flow<Tag?>
}

private const val DEFAULT_TOP_TAG_LIMIT = 10