package dev.ridill.mym.dashboard.domain.repository

import dev.ridill.mym.dashboard.domain.model.RecentTransaction
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {

    fun getMonthlyLimit(): Flow<Long>

    suspend fun updateMonthlyLimit(value: Long)

    fun getExpenditureForCurrentMonth(): Flow<Double>

    fun getRecentTransactions(): Flow<List<RecentTransaction>>
}