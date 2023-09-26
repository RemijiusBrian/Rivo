package dev.ridill.rivo.dashboard.presentation

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.CurrencyUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.ExpenseListItem

data class DashboardState(
    val currency: Currency = CurrencyUtil.default,
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val monthlyBudget: Long = Long.Zero,
    val recentSpends: Map<Boolean, List<ExpenseListItem>> = emptyMap(),
    val signedInUsername: String? = null
)