package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.data.db.RivoDatabase
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel

data object AllTransactionsScreenSpec : ScreenSpec {

    override val route: String = "all_transactions"

    override val labelRes: Int = R.string.destination_all_transactions

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AllTransactionsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagInput = viewModel.tagInput.collectAsStateWithLifecycle()
        val folderSearchQuery = viewModel.folderSearchQuery.collectAsStateWithLifecycle()
        val foldersList = viewModel.foldersList.collectAsLazyPagingItems()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val hapticFeedback = LocalHapticFeedback.current

        CollectFlowEffect(viewModel.events, context, snackbarController) { event ->
            when (event) {
                is AllTransactionsViewModel.AllTransactionsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                is AllTransactionsViewModel.AllTransactionsEvent.ProvideHapticFeedback -> {
                    hapticFeedback.performHapticFeedback(event.type)
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderDetailsWithIds -> {
                    val route = FolderDetailsScreenSpec.routeWithArgs(
                        transactionFolderId = null,
                        txIds = event.transactionIds
                    )
                    navController.navigate(route)
                }
            }
        }

        AllTransactionsScreen(
            snackbarController = snackbarController,
            state = state,
            tagNameInput = { tagInput.value?.name.orEmpty() },
            tagInputColorCode = { tagInput.value?.colorCode },
            tagExclusionInput = { tagInput.value?.excluded },
            actions = viewModel,
            navigateUp = navController::navigateUp,
            isTagInputEditMode = { tagInput.value?.id != RivoDatabase.DEFAULT_ID_LONG },
            folderSearchQuery = { folderSearchQuery.value },
            foldersList = foldersList,
            navigateToAddEditTransaction = {
                navController.navigate(AddEditTransactionScreenSpec.routeWithArg(it))
            }
        )
    }
}