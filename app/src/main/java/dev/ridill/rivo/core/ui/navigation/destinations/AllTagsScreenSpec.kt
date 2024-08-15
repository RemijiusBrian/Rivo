package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.tags.presentation.allTags.AllTagsScreen
import dev.ridill.rivo.tags.presentation.allTags.AllTagsViewModel
import java.util.Currency

data object AllTagsScreenSpec : ScreenSpec {
    override val route: String = "all_tags"

    override val labelRes: Int = R.string.destination_all_tags

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AllTagsViewModel = hiltViewModel(navBackStackEntry)
        val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
        val tagsLazyPagingItems = viewModel.allTagsPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()

        AllTagsScreen(
            snackbarController = snackbarController,
            tagsLazyPagingItems = tagsLazyPagingItems,
            searchQuery = { searchQuery.value },
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAddEditTag = { navController.navigate(AddEditTagSheetSpec.routeWithArg(it)) }
        )
    }
}