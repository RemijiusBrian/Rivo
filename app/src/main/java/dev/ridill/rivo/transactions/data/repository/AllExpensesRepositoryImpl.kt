package dev.ridill.rivo.transactions.data.repository

import dev.ridill.rivo.core.data.preferences.PreferencesManager
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.transactions.data.local.TransactionDao
import dev.ridill.rivo.transactions.data.local.relations.TransactionDetails
import dev.ridill.rivo.transactions.data.toExpenseListItem
import dev.ridill.rivo.transactions.domain.model.ExpenseListItem
import dev.ridill.rivo.transactions.domain.repository.AllExpensesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AllExpensesRepositoryImpl(
    private val dao: TransactionDao,
    private val preferencesManager: PreferencesManager
) : AllExpensesRepository {
    override suspend fun deleteExpensesByIds(ids: List<Long>) = withContext(Dispatchers.IO) {
        dao.deleteMultipleTransactionsById(ids)
    }

    override fun getExpenseYearsList(paddingCount: Int): Flow<List<Int>> =
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
        dao.getExpenditureForMonth(date.atStartOfDay())

    override fun getExpenseForDateByTag(
        date: LocalDate,
        tagId: Long?,
        showExcluded: Boolean
    ): Flow<List<ExpenseListItem>> = dao.getTransactionsListForMonth(
        monthAndYear = date.atStartOfDay(),
        transactionDirectionName = null,
        tagId = tagId,
        showExcluded = showExcluded
    ).map { it.map(TransactionDetails::toExpenseListItem) }

    override fun getShowExcludedExpenses(): Flow<Boolean> =
        preferencesManager.preferences.map { it.showExcludedExpenses }
            .distinctUntilChanged()

    override suspend fun toggleShowExcludedExpenses(show: Boolean) =
        preferencesManager.updateShowExcludedExpenses(show)

    override suspend fun toggleExpenseExclusionByIds(ids: List<Long>, excluded: Boolean) =
        withContext(Dispatchers.IO) {
            dao.toggleExclusionByIds(ids, excluded)
        }
}