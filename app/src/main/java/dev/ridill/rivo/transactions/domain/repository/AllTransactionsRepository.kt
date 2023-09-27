package dev.ridill.rivo.transactions.domain.repository

import android.icu.util.Currency
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AllTransactionsRepository {
    fun getCurrencyPreference(): Flow<Currency>
    suspend fun deleteTransactionsByIds(ids: List<Long>)
    fun getTransactionYearsList(paddingCount: Int = DEFAULT_YEAR_LIST_PADDING): Flow<List<Int>>
    fun getTotalExpenditureForDate(date: LocalDate): Flow<Double>
    fun getTransactionsForDateByTag(
        date: LocalDate,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>>

    fun getShowExcludedTransactions(): Flow<Boolean>
    suspend fun toggleShowExcludedTransactions(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: List<Long>, excluded: Boolean)
}

private const val DEFAULT_YEAR_LIST_PADDING = 5