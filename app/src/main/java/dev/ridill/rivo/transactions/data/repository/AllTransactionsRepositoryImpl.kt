package dev.ridill.rivo.transactions.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.insertSeparators
import androidx.paging.map
import androidx.room.withTransaction
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.UtilConstants
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
import dev.ridill.rivo.transactions.data.local.views.TransactionDetailsView
import dev.ridill.rivo.transactions.data.toTransactionListItem
import dev.ridill.rivo.transactions.domain.model.TransactionListItemUIModel
import dev.ridill.rivo.transactions.domain.model.TransactionType
import dev.ridill.rivo.transactions.domain.repository.AllTransactionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency
import kotlin.math.absoluteValue

class AllTransactionsRepositoryImpl(
    private val db: RivoDatabase,
    private val dao: TransactionDao,
    private val preferencesManager: PreferencesManager,
    private val currencyPrefRepo: CurrencyPreferenceRepository
) : AllTransactionsRepository {
    override fun getCurrencyPreference(date: LocalDate): Flow<Currency> = currencyPrefRepo
        .getCurrencyPreferenceForMonth(date)

    override suspend fun deleteTransactionsByIds(ids: Set<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getAmountAggregate(
        dateRange: Pair<LocalDate, LocalDate>?,
        type: TransactionType?,
        addExcluded: Boolean,
        tagIds: Set<Long>,
        selectedTxIds: Set<Long>
    ): Flow<Double> = dao.getAmountAggregate(
        startDate = dateRange?.first,
        endDate = dateRange?.second,
        type = type,
        tagIds = tagIds.takeIf { it.isNotEmpty() },
        addExcluded = addExcluded,
        selectedTxIds = selectedTxIds.takeIf { it.isNotEmpty() }
    ).distinctUntilChanged()

    override fun getDateLimits(): Flow<Pair<LocalDate, LocalDate>> = dao.getDateLimits()
        .map { limits -> limits.minDate to limits.maxDate }
        .distinctUntilChanged()

    override fun getAllTransactionsPaged(
        dateRange: Pair<LocalDate, LocalDate>?,
        transactionType: TransactionType?,
        showExcluded: Boolean,
        tagIds: Set<Long>?,
        folderId: Long?
    ): Flow<PagingData<TransactionListItemUIModel>> = Pager(
        config = PagingConfig(UtilConstants.DEFAULT_PAGE_SIZE)
    ) {
        dao.getTransactionsPaged(
            startDate = dateRange?.first,
            endDate = dateRange?.second,
            type = transactionType,
            showExcluded = showExcluded,
            tagIds = tagIds?.takeIf { it.isNotEmpty() },
            folderId = folderId
        )
    }.flow
        .map { it.map(TransactionDetailsView::toTransactionListItem) }
        .map { pagingData ->
            pagingData.map { TransactionListItemUIModel.TransactionItem(it) }
        }
        .map { pagingData ->
            pagingData
                .insertSeparators<TransactionListItemUIModel.TransactionItem, TransactionListItemUIModel>
                { before, after ->
                    if (before?.transaction?.timestamp
                            ?.withDayOfMonth(1)
                            ?.toLocalDate()
                        != after?.transaction?.timestamp
                            ?.withDayOfMonth(1)
                            ?.toLocalDate()
                    ) after?.transaction?.timestamp
                        ?.withDayOfMonth(1)
                        ?.toLocalDate()
                        ?.let { localDate ->
                            TransactionListItemUIModel.DateSeparator(localDate)
                        } else null
                }
        }

    override suspend fun setTagIdToTransactions(
        tagId: Long?,
        transactionIds: Set<Long>
    ) = withContext(Dispatchers.IO) {
        dao.setTagIdToTransactionsByIds(tagId = tagId, ids = transactionIds)
    }

    override fun getShowExcludedOption(): Flow<Boolean> = preferencesManager.preferences
        .map { it.allTransactionsShowExcludedOption }
        .distinctUntilChanged()

    override suspend fun toggleShowExcludedOption(show: Boolean) =
        preferencesManager.updateAllTransactionsShowExcludedOption(show)

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

    override suspend fun aggregateIntoSingleNewTransactions(
        ids: Set<Long>,
        dateTime: LocalDateTime
    ): Long = withContext(Dispatchers.IO) {
        db.withTransaction {
            val aggregatedAmount = dao.getAggregateAmountByIds(ids)
            var insertedId = -1L
            if (aggregatedAmount != Double.Zero) {
                val type = if (aggregatedAmount > 0) TransactionType.DEBIT
                else TransactionType.CREDIT
                val entity = TransactionEntity(
                    note = String.Empty,
                    amount = aggregatedAmount.absoluteValue,
                    timestamp = dateTime,
                    type = type,
                    isExcluded = false,
                    tagId = null,
                    folderId = null,
                    scheduleId = null
                )
                insertedId = dao.upsert(entity).first()
            }
            dao.deleteMultipleTransactionsById(ids)
            insertedId
        }
    }
}