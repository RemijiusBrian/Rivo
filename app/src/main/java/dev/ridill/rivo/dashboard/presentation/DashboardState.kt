package dev.ridill.rivo.dashboard.presentation

import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.schedules.domain.model.UpcomingSchedule
import dev.ridill.rivo.transactions.domain.model.TransactionListItem

data class DashboardState(
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val creditAmount: Double = Double.Zero,
    val monthlyBudgetInclCredits: Double = Double.Zero,
    val upcomingSchedules: List<UpcomingSchedule> = emptyList(),
    val recentSpends: List<TransactionListItem> = emptyList(),
    val signedInUsername: String? = null
)