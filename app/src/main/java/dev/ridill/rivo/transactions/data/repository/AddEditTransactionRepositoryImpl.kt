package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.toTransaction
import dev.ridill.rivo.transactions.domain.model.Transaction
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToLong

class AddEditTransactionRepositoryImpl(
    private val dao: TransactionDao
) : AddEditTransactionRepository {
    override suspend fun getTransactionById(id: Long): Transaction? = withContext(Dispatchers.IO) {
        dao.getTransactionById(id)?.toTransaction()
    }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getTransactionAmountRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override suspend fun saveTransaction(
        id: Long?,
        amount: Double,
        note: String,
        dateTime: LocalDateTime,
        transactionType: TransactionType,
        tagId: Long?,
        groupId: Long?,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id ?: Long.Zero,
            note = note,
            amount = amount,
            timestamp = dateTime,
            typeName = transactionType.name,
            tagId = tagId,
            isExcluded = excluded,
            groupId = groupId
        )
        dao.insert(entity).first()
    }

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTransactionById(id)
    }

    override suspend fun toggleExclusionById(id: Long, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(listOf(id), excluded)
        }
}