package dev.ridill.rivo.transactions.domain.repository

import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency

interface AllTransactionsRepository {
    fun getCurrencyPreference(date: LocalDate): Flow<Currency>
    suspend fun deleteTransactionsByIds(ids: Set<Long>)
    fun getTransactionYearsList(): Flow<List<Int>>
    fun getAmountAggregate(
        date: LocalDate?,
        type: TransactionType?,
        tagId: Long?,
        addExcluded: Boolean,
        selectedTxIds: Set<Long>?
    ): Flow<Double>

    fun getAllTransactionsList(
        date: LocalDate,
        tagId: Long?,
        transactionType: TransactionType?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>>

    fun getShowExcludedOption(): Flow<Boolean>
    suspend fun toggleShowExcludedOption(show: Boolean)
    suspend fun toggleTransactionExclusionByIds(ids: Set<Long>, excluded: Boolean)
    suspend fun addTransactionsToFolderByIds(ids: Set<Long>, folderId: Long)
    suspend fun removeTransactionsFromFolders(ids: Set<Long>)
    suspend fun aggregateIntoSingleNewTransactions(
        ids: Set<Long>,
        dateTime: LocalDateTime
    ): Long
}