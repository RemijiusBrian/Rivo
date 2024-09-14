package dev.ridill.rivo.core.ui.navigation.bottomSheetDestination

import androidx.compose.runtime.Composable
import androidx.navigation.FloatingWindow
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.get


class RivoBottomSheetNavigator : Navigator<RivoBottomSheetNavigator.Destination>() {

    val backStack
        get() = state.backStack

    val transitionInProgress
        get() = state.transitionsInProgress

    fun dismiss(navBackStackEntry: NavBackStackEntry) {
        popBackStack(navBackStackEntry, false)
    }

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ) {
        entries.forEach { entry -> state.push(entry) }
    }

    override fun createDestination(): Destination = Destination(this) {}

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        state.popWithTransition(popUpTo, savedState)
        // When popping, the incoming dialog is marked transitioning to hold it in
        // STARTED. With pop complete, we can remove it from transition so it can move to RESUMED.
        val popIndex = state.transitionsInProgress.value.indexOf(popUpTo)
        // do not mark complete for entries up to and including popUpTo
        state.transitionsInProgress.value.forEachIndexed { index, entry ->
            if (index > popIndex) onTransitionComplete(entry)
        }
    }

    fun onTransitionComplete(navBackStackEntry: NavBackStackEntry) {
        state.markTransitionComplete(navBackStackEntry)
    }

    class Destination(
        navigator: RivoBottomSheetNavigator,
        content: @Composable (NavBackStackEntry) -> Unit
    ) : NavDestination(navigator), FloatingWindow
}

fun NavGraphBuilder.bottomSheet(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    destination(
        BottomSheetNavigationDestinationBuilder(
            provider[RivoBottomSheetNavigator::class],
            route,
            content
        )
            .apply {
                arguments.forEach { (argumentName, argument) -> argument(argumentName, argument) }
                deepLinks.forEach { deepLink -> deepLink(deepLink) }
            }
    )
}