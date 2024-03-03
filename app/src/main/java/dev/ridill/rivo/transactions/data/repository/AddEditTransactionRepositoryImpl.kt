package dev.ridill.rivo.transactions.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.toEntity
import dev.ridill.rivo.transactions.data.toTransactionInput
import dev.ridill.rivo.transactions.domain.model.TransactionInput
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AddEditTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import kotlin.math.roundToLong

class AddEditTransactionRepositoryImpl(
    private val dao: TransactionDao,
    private val currencyRepo: CurrencyRepository
) : AddEditTransactionRepository {
    override fun getCurrencyPreference(dateTime: LocalDateTime): Flow<Currency> = currencyRepo
        .getCurrencyCodeForDateOrNext(
            date = dateTime.toLocalDate()
        )
        .distinctUntilChanged()

    override suspend fun getTransactionById(id: Long): TransactionInput? =
        withContext(Dispatchers.IO) {
            dao.getTransactionById(id)?.toTransactionInput()
        }

    override fun getAmountRecommendations(): Flow<List<Long>> = dao.getTransactionAmountRange()
        .map { (upperLimit, lowerLimit) ->
            val roundUpper = (upperLimit.roundToLong() / 10) * 10
            val roundLower = (lowerLimit.roundToLong() / 10) * 10

            val range = roundUpper - roundLower

            if (range == Long.Zero) listOf(50L, 100L, 500L)
            else listOf(roundLower, roundLower + (range / 2), roundUpper)
        }

    override suspend fun saveTransaction(transaction: TransactionInput): Long =
        withContext(Dispatchers.IO) {
            dao.insert(transaction.toEntity()).first()
        }

    override suspend fun saveTransaction(
        id: Long?,
        amount: Double,
        note: String,
        timestamp: LocalDateTime,
        transactionType: TransactionType,
        tagId: Long?,
        folderId: Long?,
        excluded: Boolean
    ): Long = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = id ?: Long.Zero,
            note = note,
            amount = amount,
            timestamp = timestamp,
            typeName = transactionType.name,
            tagId = tagId,
            isExcluded = excluded,
            folderId = folderId
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