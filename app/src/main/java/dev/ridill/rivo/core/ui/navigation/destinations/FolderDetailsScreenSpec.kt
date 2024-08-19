package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsScreen
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsViewModel
import java.util.Currency

data object FolderDetailsScreenSpec : ScreenSpec {
    override val route: String = "folder_details/{$ARG_FOLDER_ID}"
    override val labelRes: Int = R.string.destination_folder_details

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_FOLDER_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = NavDestination.ARG_INVALID_ID_LONG
        }
    )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    fun routeWithArgs(folderId: Long): String = route
        .replace(
            oldValue = "{$ARG_FOLDER_ID}",
            newValue = folderId.toString()
        )

    fun getFolderIdArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_FOLDER_ID) ?: NavDestination.ARG_INVALID_ID_LONG

    fun getFolderIdArg(navBackStackEntry: NavBackStackEntry): Long? =
        navBackStackEntry.arguments?.getLong(ARG_FOLDER_ID)

    const val ACTION_FOLDER_DETAILS = "ACTION_FOLDER_DETAILS"
    const val RESULT_FOLDER_DELETED = "RESULT_FOLDER_DELETED"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: FolderDetailsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionPagingItems = viewModel.transactionPagingData.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is FolderDetailsViewModel.FolderDetailsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                FolderDetailsViewModel.FolderDetailsEvent.FolderDeleted -> {
                    navController.navigateUpWithResult(
                        ACTION_FOLDER_DETAILS,
                        RESULT_FOLDER_DELETED
                    )
                }

                is FolderDetailsViewModel.FolderDetailsEvent.TransactionRemovedFromGroup -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.transaction_removed_from_folder),
                        actionLabel = context.getString(R.string.action_undo),
                        onSnackbarResult = {
                            if (it == SnackbarResult.ActionPerformed) {
                                viewModel.onRemoveTransactionUndo(event.transaction)
                            }
                        }
                    )
                }
            }
        }

        FolderDetailsScreen(
            appCurrencyPreference = appCurrencyPreference,
            snackbarController = snackbarController,
            transactionPagingItems = transactionPagingItems,
            state = state,
            actions = viewModel,
            navigateToAddEditTransaction = { transactionId ->
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = transactionId
                    )
                )
            },
            navigateToEditFolder = {
                getFolderIdArg(navBackStackEntry)?.let {
                    navController.navigate(
                        AddEditFolderSheetSpec.routeWithArg(it)
                    )
                }
            },
            navigateUp = navController::navigateUp
        )
    }
}

private const val ARG_FOLDER_ID = "ARG_FOLDER_ID"