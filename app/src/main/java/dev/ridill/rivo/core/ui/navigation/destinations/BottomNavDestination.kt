package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.annotation.DrawableRes

sealed interface BottomNavDestination : NavDestination {
    companion object {
        val bottomNavDestinations: List<BottomNavDestination>
            get() = NavDestination.allDestinations
                .filterIsInstance<BottomNavDestination>()
    }

    @get:DrawableRes
    val iconRes: Int
}