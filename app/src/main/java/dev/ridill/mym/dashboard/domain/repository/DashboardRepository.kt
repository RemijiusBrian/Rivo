package dev.ridill.mym.dashboard.domain.repository

import dev.ridill.mym.expense.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getMonthlyLimit(): Flow<Long>

    suspend fun updateMonthlyLimit(value: Long)

    fun getExpenditureForCurrentMonth(): Flow<Double>

    fun getRecentSpends(): Flow<List<ExpenseListItem>>
}