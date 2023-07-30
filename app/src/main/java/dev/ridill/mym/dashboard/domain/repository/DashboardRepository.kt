package dev.ridill.mym.dashboard.domain.repository

import dev.ridill.mym.dashboard.domain.model.RecentSpend
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {

    suspend fun isAppFirstLaunch(): Boolean

    suspend fun disableAppFirstLaunch()

    fun getMonthlyLimit(): Flow<Long>

    suspend fun updateMonthlyLimit(value: Long)

    fun getExpenditureForCurrentMonth(): Flow<Double>

    fun getRecentSpends(): Flow<List<RecentSpend>>
}