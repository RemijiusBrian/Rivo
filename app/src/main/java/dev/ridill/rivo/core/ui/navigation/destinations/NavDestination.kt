package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.annotation.StringRes

sealed interface NavDestination {
    companion object {
        val allDestinations: List<NavDestination>
            get() = listOf(
                OnboardingScreenSpec,
                DashboardScreenSpec,
                AddEditTransactionScreenSpec,
                AllTransactionsScreenSpec,
                SettingsGraphSpec,
                TransactionFoldersGraph,
//                BudgetPlansGraphSpec
            )
    }

    val route: String

    @get:StringRes
    val labelRes: Int
}

const val DEEP_LINK_URI = "https://www.rivo.com"
const val ARG_INVALID_ID_LONG = -1L