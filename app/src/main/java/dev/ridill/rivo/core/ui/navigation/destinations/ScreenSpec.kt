package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController

sealed interface ScreenSpec : NavDestination {
    val arguments: List<NamedNavArgument>
        get() = emptyList()

    val deepLinks: List<NavDeepLink>
        get() = emptyList()

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        get() = { fadeIn() }

    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        get() = { fadeOut() }

    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        get() = { fadeIn() }

    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        get() = { fadeOut() }

    @Composable
    fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry)
}