package dev.ridill.mym.expense.data.repository

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

    override suspend fun assignTagToExpenses(tagName: String, ids: List<Long>) =
        withContext(Dispatchers.IO) {
            dao.assignTagToExpensesWithIds(tagName, ids)
        }

    override suspend fun untagExpenses(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.untagExpenses(ids)
    }
}