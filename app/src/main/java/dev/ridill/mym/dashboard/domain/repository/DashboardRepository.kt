package dev.ridill.mym.dashboard.domain.repository

import dev.ridill.mym.expense.domain.model.ExpenseListItem
import kotlinx.coroutines.flow.Flow
import java.util.Currency

interface DashboardRepository {
    fun getCurrencyPreference(): Flow<Currency>
    fun getCurrentBudget(): Flow<Long>
    fun getExpenditureForCurrentMonth(): Flow<Double>
    fun getRecentSpends(): Flow<List<ExpenseListItem>>
}