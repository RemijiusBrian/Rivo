package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.SnackbarResult
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
import dev.ridill.rivo.core.ui.components.FloatingWindowNavigationResultEffect
import dev.ridill.rivo.core.ui.components.NavigationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel

data object AllTransactionsScreenSpec : ScreenSpec {

    override val route: String
        get() = "all_transactions"

    override val labelRes: Int
        get() = R.string.destination_all_transactions

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AllTransactionsViewModel = hiltViewModel(navBackStackEntry)
        val tagInfoLazyPagingItems = viewModel.tagInfoPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val hapticFeedback = LocalHapticFeedback.current

        FloatingWindowNavigationResultEffect(
            resultKey = FolderSelectionSheetSpec.SELECTED_FOLDER_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context,
            onResult = viewModel::onFolderSelect
        )

        FloatingWindowNavigationResultEffect(
            resultKey = TagSelectionSheetSpec.SELECTED_TAG_IDS,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context,
            onResult = viewModel::onTagSelectionResult
        )

        NavigationResultEffect(
            resultKey = AddEditTxResult::name.name,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            onResult = viewModel::onAddEditTxNavResult
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

                AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderSelection -> {
                    navController.navigate(FolderSelectionSheetSpec.routeWithArgs(null))
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToTagSelection -> {
                    navController.navigate(
                        TagSelectionSheetSpec.routeWithArgs(
                            event.multiSelection,
                            event.preSelectedIds
                        )
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.ScheduleSaved -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.schedule_saved),
                        actionLabel = context.getString(R.string.action_view),
                        onSnackbarResult = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navController.navigate(SchedulesGraphSpec.route)
                            }
                        }
                    )
                }
            }
        }

        AllTransactionsScreen(
            snackbarController = snackbarController,
            tagsPagingItems = tagInfoLazyPagingItems,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAllTags = { navController.navigate(AllTagsScreenSpec.route) },
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