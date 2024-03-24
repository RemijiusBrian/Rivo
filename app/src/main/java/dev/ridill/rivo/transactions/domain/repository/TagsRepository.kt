package dev.ridill.rivo.transactions.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TagSelector
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TagsRepository {
    fun getTagSelectorsPagingData(): Flow<PagingData<TagSelector>>
    fun getTagsPagingData(): Flow<PagingData<Tag>>
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
}