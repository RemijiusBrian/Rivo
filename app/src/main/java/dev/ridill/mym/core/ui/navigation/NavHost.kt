package dev.ridill.mym.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.ridill.mym.core.ui.navigation.destinations.DashboardScreenSpec
import dev.ridill.mym.core.ui.navigation.destinations.NavDestination
import dev.ridill.mym.core.ui.navigation.destinations.NavGraphSpec
import dev.ridill.mym.core.ui.navigation.destinations.ScreenSpec
import dev.ridill.mym.core.ui.navigation.destinations.WelcomeFlowScreenSpec

@Composable
fun MYMNavHost(
    navController: NavHostController,
    showWelcomeFlow: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (showWelcomeFlow) WelcomeFlowScreenSpec.route
        else DashboardScreenSpec.route,
        modifier = modifier
    ) {
        require(NavDestination.allDestinations.isNotEmpty()) {
            "NavGraph must contain at least 1 child destination"
        }
        NavDestination.allDestinations.forEach { destination ->
            when (destination) {
                is NavGraphSpec -> addGraphSpec(destination, navController)
                is ScreenSpec -> addScreenSpec(destination, navController)
            }
        }
    }
}

private fun NavGraphBuilder.addScreenSpec(
    screenSpec: ScreenSpec,
    navController: NavHostController
) {
    composable(
        route = screenSpec.route,
        arguments = screenSpec.arguments,
        deepLinks = screenSpec.deepLinks,
        enterTransition = screenSpec.enterTransition,
        exitTransition = screenSpec.exitTransition,
        popEnterTransition = screenSpec.popEnterTransition,
        popExitTransition = screenSpec.popExitTransition
    ) { navBackStackEntry ->
        screenSpec.Content(navController = navController, navBackStackEntry = navBackStackEntry)
    }
}

private fun NavGraphBuilder.addGraphSpec(
    navGraphSpec: NavGraphSpec,
    navController: NavHostController
) {
    require(navGraphSpec.children.isNotEmpty()) {
        "NavGraph must contain at least 1 child destination"
    }
    navigation(
        startDestination = navGraphSpec.startDestination.route,
        route = navGraphSpec.route
    ) {
        navGraphSpec.children.forEach { destination ->
            when (destination) {
                is ScreenSpec -> addScreenSpec(destination, navController)
                is NavGraphSpec -> addGraphSpec(destination, navController)
            }
        }
    }
}