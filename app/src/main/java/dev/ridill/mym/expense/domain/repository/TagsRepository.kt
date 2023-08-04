package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import kotlinx.coroutines.flow.Flow

interface TagsRepository {
    fun getAllTags(): Flow<List<ExpenseTag>>

    fun getTagsWithExpenditures(
        monthAndYearString: String,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>>
}