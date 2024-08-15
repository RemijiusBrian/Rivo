package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
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
import dev.ridill.rivo.core.ui.components.NavigationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsScreen
import dev.ridill.rivo.transactions.presentation.allTransactions.AllTransactionsViewModel
import java.util.Currency

data object AllTransactionsScreenSpec : ScreenSpec {

    override val route: String = "all_transactions"

    override val labelRes: Int = R.string.destination_all_transactions

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { -it } }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { -it } }

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AllTransactionsViewModel = hiltViewModel(navBackStackEntry)
        val tagInfoLazyPagingItems = viewModel.tagInfoPagingData.collectAsLazyPagingItems()
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val hapticFeedback = LocalHapticFeedback.current

        /*NavigationResultEffect(
            key = FolderDetailsScreenSpec.ACTION_NEW_FOLDER_CREATE,
            navBackStackEntry = navBackStackEntry,
            keys = arrayOf(viewModel),
            onResult = viewModel::onFolderSelect
        )*/

        NavigationResultEffect<Long?>(
            key = FolderSelectionSheetSpec.SELECTED_FOLDER_ID,
            navBackStackEntry = navBackStackEntry,
            keys = arrayOf(viewModel),
            onResult = viewModel::onFolderSelect
        )

        NavigationResultEffect<Set<Long>>(
            key = TagSelectionSheetSpec.SELECTED_IDS,
            navBackStackEntry = navBackStackEntry
        ) { ids ->
            ids?.firstOrNull()?.let(viewModel::onTagSelectionResultToAssignTag)
        }

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
                            folderId = null,
                            txIds = event.transactionIds
                        )
                    )
                }

                AllTransactionsViewModel.AllTransactionsEvent.NavigateToFolderSelection -> {
                    navController.navigate(FolderSelectionSheetSpec.route)
                }

                is AllTransactionsViewModel.AllTransactionsEvent.NavigateToTagSelection -> {
                    navController.navigate(TagSelectionSheetSpec.routeWithArgs(event.multiSelection))
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