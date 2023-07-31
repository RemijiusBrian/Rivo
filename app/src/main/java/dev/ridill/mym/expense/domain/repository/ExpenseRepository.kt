package dev.ridill.mym.expense.domain.repository

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.expense.domain.model.Expense
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface ExpenseRepository {

    suspend fun getExpenseById(id: Long): Expense?

    fun getAmountRecommendations(): Flow<List<Long>>

    suspend fun cacheExpense(
        amount: Double,
        note: String,
        dateTime: LocalDateTime = DateUtil.now(),
        tagId: String? = null
    )

    suspend fun deleteExpense(id: Long)
}