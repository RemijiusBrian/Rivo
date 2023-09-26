package dev.ridill.rivo.dashboard.data.repository

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.dashboard.domain.repository.DashboardRepository
import dev.ridill.rivo.expense.data.local.TransactionDao
import dev.ridill.rivo.expense.data.local.relations.TransactionWithTagRelation
import dev.ridill.rivo.expense.data.toExpenseListItem
import dev.ridill.rivo.expense.domain.model.ExpenseListItem
import dev.ridill.rivo.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val settingsRepository: SettingsRepository
) : DashboardRepository {
    override fun getCurrencyPreference(): Flow<Currency> = settingsRepository
        .getCurrencyPreference()
        .distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = settingsRepository.getCurrentBudget()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        transactionDao.getExpenditureForMonth(currentDateDbFormat())
            .distinctUntilChanged()

    override fun getRecentSpends(): Flow<Map<Boolean, List<ExpenseListItem>>> = transactionDao
        .getTransactionsForMonth(
            monthAndYear = currentDateDbFormat(),
            showExcluded = true
        ).map { entities ->
            entities
                .groupBy { it.isExcludedTransaction }
                .mapValues { it.value.map(TransactionWithTagRelation::toExpenseListItem) }
                .toSortedMap()
        }

    private fun currentDateDbFormat(): String =
        DateUtil.now().format(DateUtil.Formatters.MM_yyyy_dbFormat)
}