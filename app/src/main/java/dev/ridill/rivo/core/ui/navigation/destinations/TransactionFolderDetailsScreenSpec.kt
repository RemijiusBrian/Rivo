package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails.RESULT_FOLDER_DELETED
import dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails.TxFolderDetailsScreen
import dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails.TxFolderDetailsViewModel
import dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList.ACTION_FOLDER_DETAILS

object TransactionFolderDetailsScreenSpec : ScreenSpec {
    override val route: String =
        "transaction_folder_details/{$ARG_EXIT_AFTER_CREATE}/{$ARG_TX_FOLDER_ID}?$ARG_TX_IDS_LIST={$ARG_TX_IDS_LIST}"

    override val labelRes: Int = R.string.destination_tx_folder_details

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_EXIT_AFTER_CREATE) {
            type = NavType.BoolType
            nullable = false
            defaultValue = false
        },
        navArgument(ARG_TX_FOLDER_ID) {
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

    fun getExitAfterCreateArg(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_EXIT_AFTER_CREATE).orFalse()

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArgs(
        transactionFolderId: Long?,
        txIds: List<Long> = emptyList(),
        exitAfterClear: Boolean = false
    ): String = route
        .replace(
            oldValue = "{$ARG_EXIT_AFTER_CREATE}",
            newValue = exitAfterClear.toString()
        )
        .replace(
            oldValue = "{$ARG_TX_FOLDER_ID}",
            newValue = (transactionFolderId ?: ARG_INVALID_ID_LONG).toString()
        )
        .replace(
            oldValue = "?$ARG_TX_IDS_LIST={$ARG_TX_IDS_LIST}",
            newValue = txIds.takeIf { it.isNotEmpty() }
                ?.let {
                    buildString {
                        append("?$ARG_TX_IDS_LIST=")
                        append(it.joinToString(TX_IDS_SEPARATOR))
                    }
                }.orEmpty()
        )

    fun getFolderIdArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TX_FOLDER_ID) ?: ARG_INVALID_ID_LONG

    fun getTxIdsArgFromSavedStateHandle(savedStateHandle: SavedStateHandle): List<Long> =
        savedStateHandle.get<String>(ARG_TX_IDS_LIST)?.split(TX_IDS_SEPARATOR)
            ?.map { it.toLong() }.orEmpty()

    fun isIdInvalid(folderId: Long): Boolean = folderId == ARG_INVALID_ID_LONG

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TxFolderDetailsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val transactionsLazyPagingItems = viewModel.pager.collectAsLazyPagingItems()
        val nameInput = viewModel.folderNameInput.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        LaunchedEffect(context, snackbarController, viewModel) {
            viewModel.events.collect { event ->
                when (event) {
                    TxFolderDetailsViewModel.TxFolderDetailsEvent.NavigateUp -> {
                        navController.navigateUp()
                    }

                    is TxFolderDetailsViewModel.TxFolderDetailsEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    TxFolderDetailsViewModel.TxFolderDetailsEvent.FolderDeleted -> {
                        navController.navigateUpWithResult(
                            ACTION_FOLDER_DETAILS,
                            RESULT_FOLDER_DELETED
                        )
                    }

                    is TxFolderDetailsViewModel.TxFolderDetailsEvent.NavigateUpWithFolderId -> {
                        navController.navigateUpWithResult(
                            ACTION_NEW_FOLDER_CREATE,
                            event.folderId.toString()
                        )
                    }
                }
            }
        }

        TxFolderDetailsScreen(
            snackbarController = snackbarController,
            transactionsLazyPagingItems = transactionsLazyPagingItems,
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
private const val ARG_TX_FOLDER_ID = "ARG_TX_FOLDER_ID"
private const val ARG_TX_IDS_LIST = "ARG_TX_IDS_LIST"
private const val TX_IDS_SEPARATOR = "-"