package dev.ridill.rivo.dashboard.presentation

import android.icu.util.Currency
import dev.ridill.rivo.core.domain.util.LocaleUtil
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.transactions.domain.model.TransactionListItem

data class DashboardState(
    val currency: Currency = LocaleUtil.defaultCurrency,
    val balance: Double = Double.Zero,
    val spentAmount: Double = Double.Zero,
    val monthlyBudget: Long = Long.Zero,
    val recentSpends: List<TransactionListItem> = emptyList(),
    val signedInUsername: String? = null,
    val isAppLockEnabled: Boolean = false
)