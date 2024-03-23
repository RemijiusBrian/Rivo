package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.local.relations.TagWithExpenditureRelation
import dev.ridill.rivo.transactions.data.toTag
import dev.ridill.rivo.transactions.data.toTagInfo
import dev.ridill.rivo.transactions.domain.model.Tag
import dev.ridill.rivo.transactions.domain.model.TagInfo
import dev.ridill.rivo.transactions.domain.repository.TagsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TagsRepositoryImpl(
    private val dao: TagsDao
) : TagsRepository {

    override fun getAllTags(): Flow<List<Tag>> = dao.getAllTags()
        .map { it.map(TagEntity::toTag) }

    override suspend fun getTagById(id: Long): Tag? = withContext(Dispatchers.IO) {
        dao.getTagById(id)?.toTag()
    }

    override fun getTagsWithExpenditures(date: LocalDate): Flow<List<TagInfo>> = dao
        .getTagsWithExpenditureForDate(date.atStartOfDay())
        .map { it.map(TagWithExpenditureRelation::toTagInfo) }

    override suspend fun assignTagToTransactions(tagId: Long, ids: List<Long>) =
        withContext(Dispatchers.IO) {
            dao.assignTagToTransactionsByIds(tagId, ids)
        }

    override suspend fun untagTransactions(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.untagTransactionsByIds(ids)
    }

    override suspend fun saveTag(
        id: Long,
        name: String,
        colorCode: Int,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        val entity = TagEntity(
            id = id,
            name = name,
            colorCode = colorCode,
            createdTimestamp = timestamp,
            isExcluded = excluded
        )

        dao.insert(entity).first()
    }

    override suspend fun deleteTagById(id: Long) = withContext(Dispatchers.IO) {
        dao.untagTransactionsAndDeleteTag(id)
    }

    override suspend fun deleteTagWithTransactions(tagId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTagWithTransactions(tagId)
    }
}