package dev.ridill.mym.expense.data.repository

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.toExpenseTag
import dev.ridill.mym.expense.data.toTagWithExpenditure
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class TagsRepositoryImpl(
    private val dao: TagsDao
) : TagsRepository {

    override fun getAllTags(): Flow<List<ExpenseTag>> = dao.getAllTags()
        .map { entities ->
            entities.map(TagEntity::toExpenseTag)
        }

    override fun getTagsWithExpenditures(
        date: LocalDate,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>> = dao
        .getTagsWithExpenditureForDate(date.format(DateUtil.Formatters.MM_yyyy_dbFormat))
        .map { entities ->
            entities.map { it.toTagWithExpenditure(totalExpenditure) }
        }

    override suspend fun assignTagToExpenses(tagId: Long, ids: List<Long>) =
        withContext(Dispatchers.IO) {
            dao.assignTagToExpensesWithIds(tagId, ids)
        }

    override suspend fun deTagExpenses(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.deTagExpenses(ids)
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
        dao.untagExpensesAndDeleteTag(id)
    }

    override suspend fun deleteTagWithExpenses(tagId: Long) = withContext(Dispatchers.IO) {
        dao.deleteTagWithExpenses(tagId)
    }
}