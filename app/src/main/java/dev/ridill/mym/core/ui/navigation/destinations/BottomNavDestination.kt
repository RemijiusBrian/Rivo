package dev.ridill.mym.core.ui.navigation.destinations

import androidx.compose.ui.graphics.vector.ImageVector

sealed interface BottomNavDestination : NavDestination {
    companion object {
        val bottomNavDestinations: List<BottomNavDestination>
            get() = NavDestination.allDestinations
                .filterIsInstance<BottomNavDestination>()
    }

    val icon: ImageVector
}