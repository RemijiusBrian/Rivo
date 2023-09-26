package dev.ridill.rivo.core.ui.navigation.destinations

sealed interface NavGraphSpec : NavDestination {

    val children: List<NavDestination>

    val startDestination: NavDestination
        get() = children.first()
}