package dev.ridill.rivo.transactions.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactions.data.local.TagsDao
import dev.ridill.rivo.transactions.data.local.entity.TagEntity
import dev.ridill.rivo.transactions.data.toTransactionTag
import dev.ridill.rivo.transactions.data.toTagWithExpenditure
import dev.ridill.rivo.transactions.domain.model.TransactionTag
import dev.ridill.rivo.transactions.domain.model.TagWithExpenditure
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

    override fun getAllTags(): Flow<List<TransactionTag>> = dao.getAllTags()
        .map { entities ->
            entities.map(TagEntity::toTransactionTag)
        }

    override fun getTagsWithExpenditures(
        date: LocalDate,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>> = dao
        .getTagsWithExpenditureForDate(date.format(DateUtil.Formatters.MM_yyyy_dbFormat))
        .map { entities ->
            entities.map { it.toTagWithExpenditure(totalExpenditure) }
        }

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
        color: Color,
        excluded: Boolean,
        timestamp: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        val entity = TagEntity(
            id = id,
            name = name,
            colorCode = color.toArgb(),
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