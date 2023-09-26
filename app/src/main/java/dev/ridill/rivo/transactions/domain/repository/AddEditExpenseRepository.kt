package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.transactions.domain.model.Expense
import dev.ridill.rivo.transactions.domain.model.TransactionDirection
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AddEditExpenseRepository {
    suspend fun getExpenseById(id: Long): Expense?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun cacheExpense(
        id: Long?,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        direction: TransactionDirection = TransactionDirection.OUTGOING,
        tagId: Long?,
        excluded: Boolean = false,
        groupId: Long? = null
    ): Long

    suspend fun deleteExpense(id: Long)

    suspend fun toggleExclusionById(id: Long, excluded: Boolean)
}