package dev.ridill.rivo.core.ui.navigation.destinations

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
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
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.dashboard.presentation.DASHBOARD_ACTION_RESULT
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionScreen
import dev.ridill.rivo.transactions.presentation.addEditTransaction.AddEditTransactionViewModel
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_ADDED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_DELETED
import dev.ridill.rivo.transactions.presentation.addEditTransaction.RESULT_TRANSACTION_UPDATED

object AddEditTransactionScreenSpec : ScreenSpec {
    override val route: String =
        "add_edit_transaction/{$ARG_TRANSACTION_ID}?$ARG_LINK_FOLDER_ID={$ARG_LINK_FOLDER_ID}"

    override val labelRes: Int = R.string.destination_add_edit_transaction

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_TRANSACTION_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = ARG_INVALID_ID_LONG
        },
        navArgument(ARG_LINK_FOLDER_ID) {
            nullable = true
        }
    )

    override val deepLinks: List<NavDeepLink> = listOf(
        navDeepLink { uriPattern = AUTO_ADDED_TRANSACTION_URI_PATTERN }
    )

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInVertically { it } }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutVertically { it } }

    fun routeWithArg(
        transactionId: Long? = null,
        transactionFolderId: Long? = null
    ): String = route
        .replace(
            oldValue = "{$ARG_TRANSACTION_ID}",
            newValue = (transactionId ?: ARG_INVALID_ID_LONG).toString()
        )
        .replace(
            oldValue = "?$ARG_LINK_FOLDER_ID={$ARG_LINK_FOLDER_ID}",
            newValue = transactionFolderId?.let {
                "?$ARG_LINK_FOLDER_ID=$it"
            }.orEmpty()
        )

    fun getTransactionIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TRANSACTION_ID) ?: ARG_INVALID_ID_LONG

    fun getFolderIdToLinkFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<String?>(ARG_LINK_FOLDER_ID)?.toLongOrNull()

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_TRANSACTION_ID) != ARG_INVALID_ID_LONG

    fun isEditMode(expenseId: Long?): Boolean = expenseId != ARG_INVALID_ID_LONG

    fun buildAutoAddedTransactionDeeplinkUri(id: Long): Uri =
        AUTO_ADDED_TRANSACTION_URI_PATTERN.replace("{$ARG_TRANSACTION_ID}", id.toString()).toUri()

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: AddEditTransactionViewModel = hiltViewModel(navBackStackEntry)
        val amount = viewModel.amountInput.collectAsStateWithLifecycle(initialValue = "")
        val note = viewModel.noteInput.collectAsStateWithLifecycle(initialValue = "")
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagInput = viewModel.tagInput.collectAsStateWithLifecycle()
        val folderSearchQuery = viewModel.folderSearchQuery.collectAsStateWithLifecycle()

        val isEditMode = isArgEditMode(navBackStackEntry)

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        DestinationResultEffect(
            key = ACTION_NEW_FOLDER_CREATE,
            navBackStackEntry = navBackStackEntry,
            onResult = viewModel::onCreateFolderResult
        )

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    AddEditTransactionViewModel.AddEditTransactionEvent.TransactionAdded -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_TRANSACTION_ADDED
                        )
                    }

                    AddEditTransactionViewModel.AddEditTransactionEvent.TransactionDeleted -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_TRANSACTION_DELETED
                        )
                    }

                    AddEditTransactionViewModel.AddEditTransactionEvent.TransactionUpdated -> {
                        navController.navigateUpWithResult(
                            DASHBOARD_ACTION_RESULT,
                            RESULT_TRANSACTION_UPDATED
                        )
                    }

                    is AddEditTransactionViewModel.AddEditTransactionEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            message = event.uiText.asString(context),
                            isError = event.uiText.isErrorText
                        )
                    }

                    AddEditTransactionViewModel.AddEditTransactionEvent.NavigateToFolderDetailsForCreation -> {
                        navController.navigate(
                            FolderDetailsScreenSpec.routeWithArgs(
                                transactionFolderId = null,
                                exitAfterClear = true
                            )
                        )
                    }
                }
            }
        }

        AddEditTransactionScreen(
            snackbarController = snackbarController,
            amountInput = { amount.value },
            noteInput = { note.value },
            isEditMode = isEditMode,
            tagNameInput = { tagInput.value?.name.orEmpty() },
            tagColorInput = { tagInput.value?.colorCode },
            tagExclusionInput = { tagInput.value?.excluded },
            folderSearchQuery = { folderSearchQuery.value },
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

const val ARG_TRANSACTION_ID = "ARG_TRANSACTION_ID"
private const val ARG_LINK_FOLDER_ID = "ARG_LINK_FOLDER_ID"
private const val AUTO_ADDED_TRANSACTION_URI_PATTERN =
    "$DEEP_LINK_URI/auto_added_transaction/{$ARG_TRANSACTION_ID}"

const val ACTION_NEW_FOLDER_CREATE = "ACTION_NEW_FOLDER_CREATE"