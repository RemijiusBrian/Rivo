package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.expense.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {

    suspend fun getExpenseById(id: Long): Expense?

    fun getAmountRecommendations(): Flow<List<Long>>

    suspend fun cacheExpense(expense: Expense)

    suspend fun deleteExpense(expense: Expense)
}