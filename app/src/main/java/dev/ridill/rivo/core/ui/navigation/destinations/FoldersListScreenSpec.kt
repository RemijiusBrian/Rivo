package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.NavigationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.folders.presentation.foldersList.FoldersListScreen
import dev.ridill.rivo.folders.presentation.foldersList.FoldersListViewModel

data object FoldersListScreenSpec : ScreenSpec {
    override val route: String = "folders_list"

    override val labelRes: Int = R.string.destination_folders_list

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { -it } }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { -it } }

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: FoldersListViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val foldersPagingItems = viewModel.folderListPagingData.collectAsLazyPagingItems()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        NavigationResultEffect<Long>(
            resultKey = AddEditFolderSheetSpec.ACTION_FOLDER_SAVED,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context
        ) { id ->
            navController.navigate(
                FolderDetailsScreenSpec.routeWithArgs(id)
            )
        }

        NavigationResultEffect<String>(
            resultKey = FolderDetailsScreenSpec.ACTION_FOLDER_DETAILS,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context
        ) {
            when (it) {
                FolderDetailsScreenSpec.RESULT_FOLDER_DELETED -> R.string.transaction_folder_deleted
                else -> null
            }?.let { resId ->
                snackbarController.showSnackbar(context.getString(resId))
            }
        }

        FoldersListScreen(
            snackbarController = snackbarController,
            foldersPagingItems = foldersPagingItems,
            state = state,
            actions = viewModel,
            navigateToFolderDetails = {
                navController.navigate(
                    FolderDetailsScreenSpec.routeWithArgs(it)
                )
            },
            navigateUp = navController::navigateUp,
            navigateToAddFolder = {
                navController.navigate(AddEditFolderSheetSpec.routeWithArg())
            }
        )
    }
}