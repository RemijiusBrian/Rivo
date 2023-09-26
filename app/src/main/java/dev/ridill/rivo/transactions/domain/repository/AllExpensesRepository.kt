package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.transactions.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AllExpensesRepository {
    suspend fun deleteExpensesByIds(ids: List<Long>)
    fun getExpenseYearsList(paddingCount: Int = DEFAULT_YEAR_LIST_PADDING): Flow<List<Int>>
    fun getTotalExpenditureForDate(date: LocalDate): Flow<Double>
    fun getExpenseForDateByTag(
        date: LocalDate,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<ExpenseListItem>>

    fun getShowExcludedExpenses(): Flow<Boolean>
    suspend fun toggleShowExcludedExpenses(show: Boolean)
    suspend fun toggleExpenseExclusionByIds(ids: List<Long>, excluded: Boolean)
}

private const val DEFAULT_YEAR_LIST_PADDING = 5