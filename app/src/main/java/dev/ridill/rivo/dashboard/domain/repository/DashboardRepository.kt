package dev.ridill.rivo.dashboard.domain.repository

import android.icu.util.Currency
import dev.ridill.rivo.transactions.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow

interface DashboardRepository {
    fun getCurrencyPreference(): Flow<Currency>
    fun getCurrentBudget(): Flow<Long>
    fun getExpenditureForCurrentMonth(): Flow<Double>
    fun getRecentSpends(): Flow<Map<Boolean, List<ExpenseListItem>>>
}