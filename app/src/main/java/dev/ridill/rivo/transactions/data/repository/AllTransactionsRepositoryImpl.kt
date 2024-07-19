package dev.ridill.rivo.transactions.data.repository

import androidx.room.withTransaction
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.settings.domain.repositoty.CurrencyPreferenceRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.entity.TransactionEntity
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
        .getCurrencyPreferenceForDateOrNext(date)

    override suspend fun deleteTransactionsByIds(ids: Set<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getTransactionYearsList(): Flow<List<Int>> =
        dao.getYearsFromTransactions()
            .map { it.ifEmpty { listOf(DateUtil.now().year) } }

    override fun getAmountAggregate(
        date: LocalDate?,
        type: TransactionType?,
        tagId: Long?,
        addExcluded: Boolean,
        selectedTxIds: Set<Long>?
    ): Flow<Double> = dao.getAmountAggregate(
        date = date,
        selectedTxIds = selectedTxIds,
        tagId = tagId,
        typeName = type?.name,
        addExcluded = addExcluded
    ).distinctUntilChanged()

    override fun getAllTransactionsList(
        date: LocalDate,
        tagId: Long?,
        transactionType: TransactionType?,
        showExcluded: Boolean
    ): Flow<List<TransactionListItem>> = dao.getTransactionsList(
        date = date,
        transactionTypeName = transactionType?.name,
        tagId = tagId,
        showExcluded = showExcluded
    ).map { it.map(TransactionDetailsView::toTransactionListItem) }

    override fun getShowExcludedOption(): Flow<Boolean> =
        preferencesManager.preferences.map { it.allTransactionsShowExcludedOption }
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
                    typeName = type.name,
                    isExcluded = false,
                    tagId = null,
                    folderId = null,
                    scheduleId = null
                )
                insertedId = dao.insert(entity).first()
            }
            dao.deleteMultipleTransactionsById(ids)
            insertedId
        }
    }
}