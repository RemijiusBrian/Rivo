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
        tagId: String?
    )

    suspend fun deleteExpense(id: Long)

    suspend fun deleteExpenses(ids: List<Long>)

    fun getTotalExpenditureForDate(date: LocalDate): Flow<Double>

    fun getExpenseForDateByTag(date: LocalDate, tagId: String?): Flow<List<ExpenseListItem>>
}