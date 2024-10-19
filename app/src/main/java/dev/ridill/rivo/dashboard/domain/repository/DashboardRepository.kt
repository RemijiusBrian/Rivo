package dev.ridill.rivo.dashboard.domain.repository

import androidx.paging.PagingData
import dev.ridill.rivo.schedules.domain.model.ActiveSchedule
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getCurrentBudget(): Flow<Long>
    fun getTotalDebitsForCurrentMonth(): Flow<Double>
    fun getTotalCreditsForCurrentMonth(): Flow<Double>
    fun getSchedulesActiveThisMonth(): Flow<List<ActiveSchedule>>
    fun getRecentSpends(): Flow<PagingData<TransactionListItem>>
}