package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.toExpense
import dev.ridill.rivo.transactions.domain.model.Expense
import dev.ridill.rivo.transactions.domain.model.TransactionDirection
import dev.ridill.rivo.transactions.domain.repository.AddEditExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToLong

class AddEditExpenseRepositoryImpl(
    private val dao: TransactionDao
) : AddEditExpenseRepository {
    override suspend fun getExpenseById(id: Long): Expense? = withContext(Dispatchers.IO) {
        dao.getTransactionById(id)?.toExpense()
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getTransactionAmountRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override suspend fun cacheExpense(
        id: Long?,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        direction: TransactionDirection,
        tagId: Long?,
        excluded: Boolean,
        groupId: Long?
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id ?: Long.Zero,
            note = note,
            amount = amount,
            timestamp = dateTime,
            direction = direction.name,
            tagId = tagId,
            isExcluded = excluded,
            groupId = groupId
        )
        dao.insert(entity).first()
    }

    override suspend fun deleteExpense(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    override suspend fun toggleExclusionById(id: Long, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(listOf(id), excluded)
        }
}