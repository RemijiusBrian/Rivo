package dev.ridill.mym.dashboard.data.repository

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.dashboard.data.local.BudgetDao
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTagRelation
import dev.ridill.mym.expense.data.toRecentSpend
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) : DashboardRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetDao.getCurrentBudget()
        .map { it?.amount.orZero() }
        .distinctUntilChanged()

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        expenseDao.getExpenditureForMonth(currentDateDbFormat())
            .distinctUntilChanged()

    override fun getRecentSpends(): Flow<List<ExpenseListItem>> =
        expenseDao.getExpensesForMonth(currentDateDbFormat())
            .map { entities ->
                entities.map(ExpenseWithTagRelation::toRecentSpend)
            }

    private fun currentDateDbFormat(): String =
        DateUtil.now().format(DateUtil.Formatters.MM_yyyy_dbFormat)
}