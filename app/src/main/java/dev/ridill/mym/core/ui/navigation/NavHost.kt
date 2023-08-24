package dev.ridill.mym.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.ridill.mym.core.ui.navigation.destinations.ScreenSpec
import dev.ridill.mym.core.ui.navigation.destinations.DashboardScreenSpec
import dev.ridill.mym.core.ui.navigation.destinations.NavDestination
import dev.ridill.mym.core.ui.navigation.destinations.NavGraphSpec
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
                is NavGraphSpec -> addNavGraph(destination, navController)
                is ScreenSpec -> addChildDestination(destination, navController)
            }
        }
    }
}

private fun NavGraphBuilder.addChildDestination(
    destination: ScreenSpec,
    navController: NavHostController
) {
    composable(
        route = destination.route,
        arguments = destination.arguments,
        deepLinks = destination.deepLinks,
        enterTransition = destination.enterTransition,
        exitTransition = destination.exitTransition,
        popEnterTransition = destination.popEnterTransition,
        popExitTransition = destination.popExitTransition
    ) { navBackStackEntry ->
        destination.Content(navController = navController, navBackStackEntry = navBackStackEntry)
    }
}

private fun NavGraphBuilder.addNavGraph(
    graph: NavGraphSpec,
    navController: NavHostController
) {
    require(graph.children.isNotEmpty()) {
        "NavGraph must contain at least 1 child destination"
    }
    navigation(
        startDestination = graph.startDestination.route,
        route = graph.route
    ) {
        graph.children.forEach { destination ->
            when (destination) {
                is ScreenSpec -> addChildDestination(destination, navController)
                is NavGraphSpec -> addNavGraph(destination, navController)
            }
        }
    }
}