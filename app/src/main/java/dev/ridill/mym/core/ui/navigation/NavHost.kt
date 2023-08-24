package dev.ridill.mym.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.ridill.mym.core.ui.navigation.destinations.ChildDestinationSpec
import dev.ridill.mym.core.ui.navigation.destinations.DashboardDestinationSpec
import dev.ridill.mym.core.ui.navigation.destinations.NavDestination
import dev.ridill.mym.core.ui.navigation.destinations.NavGraphSpec
import dev.ridill.mym.core.ui.navigation.destinations.WelcomeFlowDestinationSpec

@Composable
fun MYMNavHost(
    navController: NavHostController,
    showWelcomeFlow: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (showWelcomeFlow) WelcomeFlowDestinationSpec.route
        else DashboardDestinationSpec.route,
        modifier = modifier
    ) {
        require(NavDestination.allDestinations.isNotEmpty()) {
            "NavGraph must contain at least 1 child destination"
        }
        NavDestination.allDestinations.forEach { destination ->
            when (destination) {
                is NavGraphSpec -> addNavGraph(destination, navController)
                is ChildDestinationSpec -> addChildDestination(destination, navController)
            }
        }
    }
}

private fun NavGraphBuilder.addChildDestination(
    destination: ChildDestinationSpec,
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
                is ChildDestinationSpec -> addChildDestination(destination, navController)
                is NavGraphSpec -> addNavGraph(destination, navController)
            }
        }
    }
}