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
import dev.ridill.rivo.core.domain.util.DateUtil
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel
import java.util.Currency

data object AllTransactionsScreenSpec : ScreenSpec {

    override val route: String = "all_transactions"

    override val labelRes: Int = R.string.destination_all_transactions

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AllTransactionsViewModel = hiltViewModel(navBackStackEntry)
        val tagsPagingItems = viewModel.tagsPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val hapticFeedback = LocalHapticFeedback.current

        DestinationResultEffect(
            key = FolderDetailsScreenSpec.ACTION_NEW_FOLDER_CREATE,
            navBackStackEntry = navBackStackEntry,
            keys = arrayOf(viewModel),
            onResult = viewModel::onFolderSelect
        )

        DestinationResultEffect(
            key = FolderSelectionSheetSpec.SELECTED_FOLDER_ID,
            navBackStackEntry = navBackStackEntry,
            keys = arrayOf(viewModel),
            onResult = viewModel::onFolderSelect
        )

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
                    navController.navigate(
                        route = FolderDetailsScreenSpec.routeWithArgs(
                            transactionFolderId = null,
                            txIds = event.transactionIds
                        )
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderSelection -> {
                    navController.navigate(FolderSelectionSheetSpec.route)
                }
            }
        }

        AllTransactionsScreen(
            snackbarController = snackbarController,
            tagsPagingItems = tagsPagingItems,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAddEditTransaction = { txId, selectedDate ->
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(
                        transactionId = txId,
                        initialDateTime = selectedDate?.atTime(DateUtil.now().toLocalTime())
                    )
                )
            }
        )
    }
}