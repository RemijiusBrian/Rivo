package dev.ridill.rivo.tags.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getAllTagsPagingData(
        searchQuery: String = String.Empty,
        ids: Set<Long>? = null
    ): Flow<PagingData<Tag>>

    fun getRecentTagsPagingData(
        date: LocalDate?,
        limit: Int = DEFAULT_TOP_TAG_LIMIT
    ): Flow<PagingData<Tag>>

    fun getTopTagInfoPagingData(
        date: LocalDate?,
        limit: Int = DEFAULT_TOP_TAG_LIMIT
    ): Flow<PagingData<TagInfo>>

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
    fun getTagsListFlowByIds(ids: Set<Long>): Flow<List<Tag>>
}

private const val DEFAULT_TOP_TAG_LIMIT = 10