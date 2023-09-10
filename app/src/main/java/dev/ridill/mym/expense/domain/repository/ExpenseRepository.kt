package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime

interface ExpenseRepository {
    suspend fun getExpenseById(id: Long): Expense?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun cacheExpense(
        id: Long?,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        tagId: Long?
    ): Long

    suspend fun deleteExpense(id: Long)
    suspend fun deleteExpenses(ids: List<Long>)
    fun getExpenseYearsList(paddingCount: Int = DEFAULT_YEAR_PADDING): Flow<List<Int>>
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

private const val DEFAULT_YEAR_PADDING = 5