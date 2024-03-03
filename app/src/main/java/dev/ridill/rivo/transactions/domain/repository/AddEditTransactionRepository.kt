package dev.ridill.rivo.transactions.domain.repository

import android.icu.util.Currency
import dev.ridill.rivo.transactions.domain.model.TransactionInput
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface AddEditTransactionRepository {
    fun getCurrencyPreference(dateTime: LocalDateTime): Flow<Currency>
    suspend fun getTransactionById(id: Long): TransactionInput?
    fun getAmountRecommendations(): Flow<List<Long>>
    suspend fun saveTransaction(transaction: TransactionInput): Long

    suspend fun saveTransaction(
        id: Long?,
        amount: Double,
        note: String,
        timestamp: LocalDateTime,
        transactionType: TransactionType,
        tagId: Long?,
        folderId: Long? = null,
        excluded: Boolean = false
    ): Long

    suspend fun deleteTransaction(id: Long)

    suspend fun toggleExclusionById(id: Long, excluded: Boolean)
}