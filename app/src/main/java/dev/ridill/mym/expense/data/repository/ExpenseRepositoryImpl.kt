package dev.ridill.mym.expense.data.repository

import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.toEntity
import dev.ridill.mym.expense.data.toExpense
import dev.ridill.mym.expense.domain.model.Expense
import dev.ridill.mym.expense.domain.repository.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {
    override suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        dao.getExpenseById(id)?.toExpense()
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getExpenseRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override suspend fun cacheExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.insert(expense.toEntity())
    }

    override suspend fun deleteExpense(expense: Expense) = withContext(Dispatchers.IO) {
        dao.delete(expense.toEntity())
    }
}