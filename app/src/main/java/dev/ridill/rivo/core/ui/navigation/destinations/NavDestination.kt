package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.annotation.StringRes

sealed interface NavDestination {
    companion object {
        val allDestinations: List<NavDestination>
            get() = listOf(
                OnboardingScreenSpec,
                DashboardScreenSpec,
                AddEditTransactionGraphSpec,
                AllTransactionsScreenSpec,
                FoldersGraph,
                TagsGraph,
                SchedulesGraphSpec,
                SettingsGraphSpec,
                FolderSelectionSheetSpec,
                AddEditTagSheetSpec,
                TagSelectionSheetSpec,
            )

        const val DEEP_LINK_URI = "dev.ridill.rivo://app"
        const val ARG_INVALID_ID_LONG = -1L
    }

    val route: String

    @get:StringRes
    val labelRes: Int
}