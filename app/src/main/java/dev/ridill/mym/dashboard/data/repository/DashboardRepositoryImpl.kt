package dev.ridill.mym.dashboard.data.repository

import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.dashboard.domain.model.RecentTransaction
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.entity.ExpenseEntity
import dev.ridill.mym.expense.data.toRecentTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DashboardRepositoryImpl(
    private val dao: ExpenseDao,
    private val preferencesManager: PreferencesManager
) : DashboardRepository {
    override fun getMonthlyLimit(): Flow<Long> = preferencesManager.preferences
        .map { it.monthlyLimit }
        .distinctUntilChanged()

    override suspend fun updateMonthlyLimit(value: Long) {
        preferencesManager.updateMonthlyLimit(value)
    }

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        dao.getExpenditureForMonth(DateUtil.currentMonthYear())
            .distinctUntilChanged()

    override fun getRecentTransactions(): Flow<List<RecentTransaction>> =
        dao.getExpensesForMonth(DateUtil.currentMonthYear()).map { entities ->
            entities.map(ExpenseEntity::toRecentTransaction)
        }
}