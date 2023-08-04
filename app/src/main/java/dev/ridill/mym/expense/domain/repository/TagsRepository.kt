package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.ExpenseTag
import dev.ridill.mym.expense.domain.model.TagWithExpenditure
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TagsRepository {
    fun getAllTags(): Flow<List<ExpenseTag>>

    fun getTagsWithExpenditures(
        date: LocalDate,
        totalExpenditure: Double
    ): Flow<List<TagWithExpenditure>>
}