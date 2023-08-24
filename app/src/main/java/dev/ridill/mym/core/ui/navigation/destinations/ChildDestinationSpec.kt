package dev.ridill.mym.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import dev.ridill.mym.core.ui.components.simpleFadeIn
import dev.ridill.mym.core.ui.components.simpleFadeOut

sealed interface ChildDestinationSpec : NavDestination {
    val arguments: List<NamedNavArgument>
        get() = emptyList()

    val deepLinks: List<NavDeepLink>
        get() = emptyList()

    val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        get() = { simpleFadeIn() }

    val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        get() = { simpleFadeOut() }

    val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?
        get() = { simpleFadeIn() }

    val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?
        get() = { simpleFadeOut() }

    @Composable
    fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry)
}