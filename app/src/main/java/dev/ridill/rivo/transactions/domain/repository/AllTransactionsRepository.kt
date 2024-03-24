package dev.ridill.rivo.transactions.domain.repository

import android.icu.util.Currency
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AllTransactionsRepository {
    fun getCurrencyPreference(date: LocalDate): Flow<Currency>
    suspend fun deleteTransactionsByIds(ids: Set<Long>)
    fun getTransactionYearsList(): Flow<List<Int>>
    fun getAmountAggregate(
        date: LocalDate,
        type: TransactionType?,
        tagId: Long?,
        addExcluded: Boolean,
        selectedTxIds: Set<Long>?
    ): Flow<Double>

    fun getTransactionsForDateByTag(
        date: LocalDate,
        tagId: Long?,
        transactionType: TransactionType?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>>

    fun getShowExcludedTransactions(): Flow<Boolean>
    suspend fun toggleShowExcludedTransactions(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: Set<Long>, excluded: Boolean)
    suspend fun addTransactionsToFolderByIds(ids: Set<Long>, folderId: Long)
    suspend fun removeTransactionsFromFolders(ids: Set<Long>)
}