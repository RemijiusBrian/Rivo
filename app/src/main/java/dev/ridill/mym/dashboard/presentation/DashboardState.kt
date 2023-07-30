package dev.ridill.mym.dashboard.presentation

import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.dashboard.domain.model.RecentTransaction

data class DashboardState(
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val monthlyLimit: Long = Long.Zero,
    val recentTransactions: List<RecentTransaction> = emptyList(),
    val showLimitInput: Boolean = false,
    val isLimitInputError: Boolean = false
)