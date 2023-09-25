package dev.ridill.mym.dashboard.data.repository

import android.icu.util.Currency
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTagRelation
import dev.ridill.mym.expense.data.toExpenseListItem
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val expenseDao: ExpenseDao,
    private val settingsRepository: SettingsRepository
) : DashboardRepository {
    override fun getCurrencyPreference(): Flow<Currency> = settingsRepository
        .getCurrencyPreference()
        .distinctUntilChanged()

    override fun getCurrentBudget(): Flow<Long> = settingsRepository.getCurrentBudget()
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        expenseDao.getExpenditureForMonth(currentDateDbFormat())
            .distinctUntilChanged()

    override fun getRecentSpends(): Flow<Map<Boolean, List<ExpenseListItem>>> = expenseDao
        .getExpensesForMonth(
            monthAndYear = currentDateDbFormat(),
            showExcluded = true
        ).map { entities ->
            entities
                .groupBy { it.isExcludedTransaction }
                .mapValues { it.value.map(ExpenseWithTagRelation::toExpenseListItem) }
        }

    private fun currentDateDbFormat(): String =
        DateUtil.now().format(DateUtil.Formatters.MM_yyyy_dbFormat)
}