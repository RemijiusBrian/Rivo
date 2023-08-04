package dev.ridill.mym.expense.data.repository

import dev.ridill.mym.expense.data.local.TagsDao
import dev.ridill.mym.expense.data.local.entity.TagEntity
import dev.ridill.mym.expense.data.toExpenseTag
import dev.ridill.mym.expense.data.toTagWithExpenditure
import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import dev.ridill.mym.expense.domain.repository.TagsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagsRepositoryImpl(
    private val dao: TagsDao
) : TagsRepository {

    override fun getAllTags(): Flow<List<ExpenseTag>> = dao.getAllTags()
        .map { entities ->
            entities.map(TagEntity::toExpenseTag)
        }

    override fun getTagsWithExpenditures(
        monthAndYearString: String,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>> = dao.getTagsWithExpenditureForDate(monthAndYearString)
        .map { entities ->
            entities.map { it.toTagWithExpenditure(totalExpenditure) }
        }
}