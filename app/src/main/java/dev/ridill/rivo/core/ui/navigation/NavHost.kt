package dev.ridill.rivo.core.ui.navigation

import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material.navigation.bottomSheet
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navigation
import dev.ridill.rivo.core.domain.util.logD
import dev.ridill.rivo.core.ui.navigation.destinations.BottomSheetSpec
import dev.ridill.rivo.core.ui.navigation.destinations.DashboardScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.NavDestination
import dev.ridill.rivo.core.ui.navigation.destinations.NavGraphSpec
import dev.ridill.rivo.core.ui.navigation.destinations.OnboardingScreenSpec
import dev.ridill.rivo.core.ui.navigation.destinations.ScreenSpec
import java.util.Currency

@Composable
fun RivoNavHost(
    windowSizeClass: WindowSizeClass,
    bottomSheetNavigator: BottomSheetNavigator,
    navController: NavHostController,
    startOnboarding: Boolean,
    appCurrencyPreference: Currency,
    modifier: Modifier = Modifier
) {
    // Without this observation the navigation results break.
    // Hence leaving this in for now
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(key1 = currentBackStackEntry) {
        logD(NavHost::class.simpleName) { "Current BackStackEntry = ${currentBackStackEntry?.id}, route = ${currentBackStackEntry?.destination?.route}" }
    }

    ModalBottomSheetLayout(bottomSheetNavigator) {
        NavHost(
            navController = navController,
            startDestination = if (startOnboarding) OnboardingScreenSpec.route
            else DashboardScreenSpec.route,
            modifier = modifier
        ) {
            require(NavDestination.allDestinations.isNotEmpty()) {
                "NavGraph must contain at least 1 child destination"
            }

            NavDestination.allDestinations.forEach { destination ->
                when (destination) {
                    is NavGraphSpec -> addGraphSpec(
                        windowSizeClass = windowSizeClass,
                        navGraphSpec = destination,
                        navController = navController,
                        appCurrencyPreference = appCurrencyPreference
                    )

                    is ScreenSpec -> addScreenSpec(
                        windowSizeClass = windowSizeClass,
                        screenSpec = destination,
                        navController = navController,
                        appCurrencyPreference = appCurrencyPreference
                    )

                    is BottomSheetSpec -> addBottomSheetSpec(
                        windowSizeClass = windowSizeClass,
                        sheetSpec = destination,
                        navController = navController,
                        appCurrencyPreference = appCurrencyPreference
                    )
                }
            }
        }
    }
}

private fun NavGraphBuilder.addScreenSpec(
    windowSizeClass: WindowSizeClass,
    screenSpec: ScreenSpec,
    navController: NavHostController,
    appCurrencyPreference: Currency
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
        screenSpec.Content(
            windowSizeClass = windowSizeClass,
            navController = navController,
            navBackStackEntry = navBackStackEntry,
            appCurrencyPreference = appCurrencyPreference
        )
    }
}

private fun NavGraphBuilder.addBottomSheetSpec(
    windowSizeClass: WindowSizeClass,
    sheetSpec: BottomSheetSpec,
    navController: NavHostController,
    appCurrencyPreference: Currency
) {
    bottomSheet(
        route = sheetSpec.route,
        arguments = sheetSpec.arguments,
        deepLinks = sheetSpec.deepLinks,
    ) { navBackStackEntry ->
        sheetSpec.Content(
            windowSizeClass = windowSizeClass,
            navController = navController,
            navBackStackEntry = navBackStackEntry,
            appCurrencyPreference = appCurrencyPreference
        )
    }
}

private fun NavGraphBuilder.addGraphSpec(
    windowSizeClass: WindowSizeClass,
    navGraphSpec: NavGraphSpec,
    navController: NavHostController,
    appCurrencyPreference: Currency
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
                is ScreenSpec -> addScreenSpec(
                    windowSizeClass = windowSizeClass,
                    screenSpec = destination,
                    navController = navController,
                    appCurrencyPreference = appCurrencyPreference
                )

                is NavGraphSpec -> addGraphSpec(
                    windowSizeClass = windowSizeClass,
                    navGraphSpec = navGraphSpec,
                    navController = navController,
                    appCurrencyPreference = appCurrencyPreference
                )

                is BottomSheetSpec -> addBottomSheetSpec(
                    windowSizeClass = windowSizeClass,
                    sheetSpec = destination,
                    navController = navController,
                    appCurrencyPreference = appCurrencyPreference
                )
            }
        }
    }
}