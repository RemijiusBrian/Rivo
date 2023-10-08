package dev.ridill.rivo.transactions.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
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
    private val settingsRepo: SettingsRepository
) : AllTransactionsRepository {
    override fun getCurrencyPreference(): Flow<Currency> = settingsRepo.getCurrencyPreference()

    override suspend fun deleteTransactionsByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getTransactionYearsList(paddingCount: Int): Flow<List<Int>> =
        dao.getYearsFromTransactions()
            .map { years ->
                if (years.size >= paddingCount) years
                else {
                    val difference = paddingCount - years.size
                    val latestYear = years.lastOrNull() ?: (DateUtil.now().year - 1)
                    val paddingYears = ((latestYear + 1)..(latestYear + difference))
                    years + paddingYears
                }
            }

    override fun getTotalExpenditureForDate(date: LocalDate): Flow<Double> =
        dao.getAmountSum(date.atStartOfDay())

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
    ).map { it.map(TransactionDetails::toTransactionListItem) }

    override fun getShowExcludedTransactions(): Flow<Boolean> =
        preferencesManager.preferences.map { it.showExcludedTransactions }
            .distinctUntilChanged()

    override suspend fun toggleShowExcludedTransactions(show: Boolean) =
        preferencesManager.updateShowExcludedTransactions(show)

    override suspend fun toggleTransactionExclusionByIds(ids: List<Long>, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(ids, excluded)
        }

    override suspend fun addTransactionsToFolderByIds(transactionIds: List<Long>, folderId: Long) =
        withContext(Dispatchers.IO) {
            dao.setFolderIdToTransactionsByIds(transactionIds = transactionIds, folderId = folderId)
        }

    override suspend fun removeTransactionsFromFolders(ids: List<Long>) =
        withContext(Dispatchers.IO) {
            dao.removeFolderFromTransactionsByIds(ids)
        }
}