package dev.ridill.mym.dashboard.data.repository

import android.icu.util.Currency
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.TransactionDao
import dev.ridill.mym.expense.data.local.relations.TransactionWithTagRelation
import dev.ridill.mym.expense.data.toExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
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
        }

    private fun currentDateDbFormat(): String =
        DateUtil.now().format(DateUtil.Formatters.MM_yyyy_dbFormat)
}