package dev.ridill.rivo.dashboard.domain.repository

import dev.ridill.rivo.schedules.domain.model.UpcomingSchedule
import dev.ridill.rivo.transactions.domain.model.TransactionListItem
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface DashboardRepository {
    fun getCurrencyPreference(): Flow<Currency>
    fun getCurrentBudget(): Flow<Long>
    fun getExpenditureForCurrentMonth(): Flow<Double>
    fun getTotalCreditsForCurrentMonth(): Flow<Double>
    fun getActiveSchedules(): Flow<List<UpcomingSchedule>>
    fun getRecentSpends(): Flow<List<TransactionListItem>>
}