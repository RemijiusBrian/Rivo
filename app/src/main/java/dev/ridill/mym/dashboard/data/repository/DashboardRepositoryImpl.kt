package dev.ridill.mym.dashboard.data.repository

import dev.ridill.mym.core.data.preferences.PreferencesManager
import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.dashboard.domain.model.RecentSpend
import dev.ridill.mym.dashboard.domain.repository.DashboardRepository
import dev.ridill.mym.expense.data.local.ExpenseDao
import dev.ridill.mym.expense.data.local.relations.ExpenseWithTag
import dev.ridill.mym.expense.data.toRecentSpend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DashboardRepositoryImpl(
    private val dao: ExpenseDao,
    private val preferencesManager: PreferencesManager
) : DashboardRepository {
    override suspend fun isAppFirstLaunch(): Boolean = preferencesManager
        .preferences
        .first()
        .isAppFirstLaunch

    override suspend fun disableAppFirstLaunch() = withContext(Dispatchers.IO) {
        preferencesManager.disableAppFirstLaunch()
    }

    override fun getMonthlyLimit(): Flow<Long> = preferencesManager.preferences
        .map { it.monthlyLimit }
        .distinctUntilChanged()

    override suspend fun updateMonthlyLimit(value: Long) {
        preferencesManager.updateMonthlyLimit(value)
    }

    override fun getExpenditureForCurrentMonth(): Flow<Double> =
        dao.getExpenditureForMonth(DateUtil.currentMonthYear())
            .distinctUntilChanged()

    override fun getRecentSpends(): Flow<List<RecentSpend>> =
        dao.getExpensesForMonth(DateUtil.currentMonthYear()).map { entities ->
            entities.map(ExpenseWithTag::toRecentSpend)
        }
}