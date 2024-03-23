package dev.ridill.rivo.transactions.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.settings.domain.repositoty.CurrencyRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AllTransactionsRepositoryImpl(
    private val dao: TransactionDao,
    private val preferencesManager: PreferencesManager,
    private val currencyRepo: CurrencyRepository
) : AllTransactionsRepository {
    override fun getCurrencyPreference(date: LocalDate): Flow<Currency> = currencyRepo
        .getCurrencyForDateOrNext(date)

    override suspend fun deleteTransactionsByIds(ids: Set<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getTransactionYearsList(): Flow<List<Int>> =
        dao.getYearsFromTransactions()
            .map { it.ifEmpty { listOf(DateUtil.now().year) } }

    override fun getAmountAggregate(
        date: LocalDate,
        type: TransactionType?,
        addExcluded: Boolean,
        selectedTxIds: Set<Long>?
    ): Flow<Double> = dao.getAmountAggregate(
        dateTime = date.atStartOfDay(),
        selectedTxIds = selectedTxIds,
        typeName = type?.name,
        addExcluded = addExcluded
    ).distinctUntilChanged()

    override fun getTransactionsForDateByTag(
        date: LocalDate,
        tagId: Long?,
        transactionType: TransactionType?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>> = dao.getTransactionsList(
        monthAndYear = date.atStartOfDay(),
        transactionTypeName = transactionType?.name,
        tagId = tagId,
        showExcluded = showExcluded
    ).map { it.map(TransactionDetailsView::toTransactionListItem) }

    override fun getShowExcludedTransactions(): Flow<Boolean> =
        preferencesManager.preferences.map { it.showExcludedTransactions }
            .distinctUntilChanged()

    override suspend fun toggleShowExcludedTransactions(show: Boolean) =
        preferencesManager.updateShowExcludedTransactions(show)

    override suspend fun toggleTransactionExclusionByIds(ids: Set<Long>, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(ids, excluded)
        }

    override suspend fun addTransactionsToFolderByIds(ids: Set<Long>, folderId: Long) =
        withContext(Dispatchers.IO) {
            dao.setFolderIdToTransactionsByIds(ids = ids, folderId = folderId)
        }

    override suspend fun removeTransactionsFromFolders(ids: Set<Long>) =
        withContext(Dispatchers.IO) {
            dao.removeFolderFromTransactionsByIds(ids)
        }
}