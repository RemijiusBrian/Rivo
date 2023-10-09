package dev.ridill.rivo.transactions.domain.repository

import android.icu.util.Currency
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AllTransactionsRepository {
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun deleteTransactionsByIds(ids: List<Long>)
    fun getTransactionYearsList(paddingCount: Int = DEFAULT_YEAR_LIST_PADDING): Flow<List<Int>>
    fun getAmountSumForDate(
        date: LocalDate,
        type: TransactionType
    ): Flow<Double>
    fun getTransactionsForDateByTag(
        date: LocalDate,
        tagId: Long?,
        transactionType: TransactionType?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>>

    fun getShowExcludedTransactions(): Flow<Boolean>
    suspend fun toggleShowExcludedTransactions(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: List<Long>, excluded: Boolean)
    suspend fun addTransactionsToFolderByIds(transactionIds: List<Long>, folderId: Long)
    suspend fun removeTransactionsFromFolders(ids: List<Long>)
}

private const val DEFAULT_YEAR_LIST_PADDING = 5