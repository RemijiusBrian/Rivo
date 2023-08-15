package dev.ridill.mym.settings.domain.repositoty

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getCurrentBudget(): Flow<Long>
    fun getPreviousBudgets(limit: Int = DEFAULT_PREVIOUS_BUDGETS_LIMIT): Flow<List<Long>>
    suspend fun updateCurrentBudget(value: Long)
}

private const val DEFAULT_PREVIOUS_BUDGETS_LIMIT = 3