package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.navigation.NavDeepLink
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Empty
import dev.ridill.rivo.core.domain.util.NewLine
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsScreen
import dev.ridill.rivo.folders.presentation.folderDetails.FolderDetailsViewModel
import dev.ridill.rivo.folders.presentation.folderDetails.RESULT_FOLDER_DELETED
import dev.ridill.rivo.folders.presentation.foldersList.ACTION_FOLDER_DETAILS

data object FolderDetailsScreenSpec : ScreenSpec {
    override val route: String = """
        folder_details
        /{$ARG_FOLDER_ID}
        ?$ARG_EXIT_AFTER_CREATE={$ARG_EXIT_AFTER_CREATE}
        &$ARG_TX_IDS_LIST={$ARG_TX_IDS_LIST}
    """.trimIndent()
        .replace(String.NewLine, String.Empty)

    override val labelRes: Int = R.string.destination_folder_details

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_EXIT_AFTER_CREATE) {
            type = NavType.BoolType
            nullable = false
            defaultValue = false
        },
        navArgument(ARG_FOLDER_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        },
        navArgument(ARG_TX_IDS_LIST) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = ADD_FOLDER_SHORTCUT_DEEPLINK_URI }
    )

    fun getExitAfterCreateArg(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_EXIT_AFTER_CREATE).orFalse()

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArgs(
        transactionFolderId: Long?,
        exitAfterClear: Boolean = false,
        txIds: List<Long> = emptyList()
    ): String = route
        .replace(
            oldValue = "{$ARG_FOLDER_ID}",
            newValue = (transactionFolderId ?: ARG_INVALID_ID_LONG).toString()
        )
        .replace(
            oldValue = "{$ARG_EXIT_AFTER_CREATE}",
            newValue = exitAfterClear.toString()
        )
        .replace(
            oldValue = "{$ARG_TX_IDS_LIST}",
            newValue = txIds.joinToString(TX_IDS_SEPARATOR)
        )

    fun getFolderIdArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_FOLDER_ID) ?: ARG_INVALID_ID_LONG

    fun getTxIdsArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): List<Long> =
        savedStateHandle.get<String>(ARG_TX_IDS_LIST).orEmpty()
            .split(TX_IDS_SEPARATOR)
            .mapNotNull { it.toLongOrNull() }

    fun isIdInvalid(folderId: Long): Boolean = folderId == ARG_INVALID_ID_LONG

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: FolderDetailsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionsList = viewModel.transactionsList.collectAsLazyPagingItems()
        val nameInput = viewModel.folderNameInput.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                FolderDetailsViewModel.FolderDetailsEvent.NavigateUp -> {
                    navController.navigateUp()
                }

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

                is FolderDetailsViewModel.FolderDetailsEvent.NavigateUpWithFolderId -> {
                    navController.navigateUpWithResult(
                        ACTION_NEW_FOLDER_CREATE,
                        event.folderId.toString()
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
            snackbarController = snackbarController,
            transactionsList = transactionsList,
            state = state,
            folderName = { nameInput.value },
            actions = viewModel,
            navigateToAddEditTransaction = { transactionId ->
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = transactionId,
                        transactionFolderId = state.folderId
                    )
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}

private const val ARG_EXIT_AFTER_CREATE = "ARG_EXIT_AFTER_CREATE"
private const val ARG_FOLDER_ID = "ARG_FOLDER_ID"
private const val ARG_TX_IDS_LIST = "ARG_TX_IDS_LIST"
private const val TX_IDS_SEPARATOR = "-"

private const val ADD_FOLDER_SHORTCUT_DEEPLINK_URI =
    "$DEEP_LINK_URI/add_folder_shortcut"