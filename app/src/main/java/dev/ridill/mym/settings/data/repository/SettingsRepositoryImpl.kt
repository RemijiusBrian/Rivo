package dev.ridill.mym.settings.data.repository

import dev.ridill.mym.core.domain.util.DateUtil
import dev.ridill.mym.core.domain.util.orZero
import dev.ridill.mym.dashboard.data.local.BudgetDao
import dev.ridill.mym.dashboard.data.local.entity.BudgetEntity
import dev.ridill.mym.settings.domain.repositoty.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SettingsRepositoryImpl(
    private val budgetDao: BudgetDao
) : SettingsRepository {
    override fun getCurrentBudget(): Flow<Long> = budgetDao.getCurrentBudget()
        .map { it?.amount.orZero() }
        .distinctUntilChanged()

    override fun getPreviousBudgets(limit: Int): Flow<List<Long>> = budgetDao
        .getPreviousBudgets(limit)
        .map { entities ->
            entities.map { it.amount }
        }

    override suspend fun updateCurrentBudget(value: Long) {
        withContext(Dispatchers.IO) {
            val entity = BudgetEntity(
                amount = value,
                createdTimestamp = DateUtil.now(),
                isCurrent = true
            )
            budgetDao.insertAndSetCurrent(entity)
        }
    }
}