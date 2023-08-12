package dev.ridill.mym.core.ui.navigation.destinations

import androidx.annotation.DrawableRes

sealed interface BottomNavDestination : NavDestination {

    companion object {
        val bottomNavDestinations: List<BottomNavDestination> = listOf(
            SettingsGraph,
            AllExpensesDestination
        )
    }

    @get:DrawableRes
    val iconRes: Int
}