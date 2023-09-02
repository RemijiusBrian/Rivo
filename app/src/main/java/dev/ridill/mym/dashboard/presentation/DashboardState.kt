package dev.ridill.mym.dashboard.presentation

import dev.ridill.mym.core.domain.util.CurrencyUtil
import dev.ridill.mym.core.domain.util.Zero
import dev.ridill.mym.expense.domain.model.ExpenseListItem
import java.util.Currency

data class DashboardState(
    val currency: Currency = CurrencyUtil.default,
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val monthlyBudget: Long = Long.Zero,
    val recentSpends: List<ExpenseListItem> = emptyList(),
    val signedInUsername: String? = null
)