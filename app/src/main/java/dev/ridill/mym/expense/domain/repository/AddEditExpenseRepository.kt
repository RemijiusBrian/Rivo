package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.model.ExpenseTag
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AddEditExpenseRepository {

    suspend fun getExpenseById(id: Long): Expense?

    fun getAmountRecommendations(): Flow<List<Long>>

    fun getTagsList(): Flow<List<ExpenseTag>>

    suspend fun cacheExpense(
        id: Long,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        tagId: String?
    )

    suspend fun deleteExpense(id: Long)
}