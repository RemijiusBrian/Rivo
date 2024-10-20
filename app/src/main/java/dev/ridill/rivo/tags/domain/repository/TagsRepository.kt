package dev.ridill.rivo.tags.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.tags.domain.model.Tag
import dev.ridill.rivo.tags.domain.model.TagInfo
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface TagsRepository {
    fun getAllTagsPagingData(
        searchQuery: String = String.Empty,
        limit: Int = RivoDatabase.INVALID_LIMIT
    ): Flow<PagingData<Tag>>

    fun getTagInfoPagingData(
        dateRange: Pair<LocalDate, LocalDate>?,
        limit: Int = UtilConstants.DEFAULT_TAG_LIST_LIMIT
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