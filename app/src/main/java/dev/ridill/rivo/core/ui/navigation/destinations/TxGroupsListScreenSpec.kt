package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactionGroups.presentation.groupsList.TxGroupsListScreen
import dev.ridill.rivo.transactionGroups.presentation.groupsList.TxGroupsListViewModel

object TxGroupsListScreenSpec : ScreenSpec {
    override val route: String = "transaction_groups_list"

    override val labelRes: Int = R.string.destination_tx_groups_list

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TxGroupsListViewModel = hiltViewModel(navBackStackEntry)
        val groupsList by viewModel.groupsList.collectAsStateWithLifecycle(emptyList())

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        LaunchedEffect(context, snackbarController, viewModel) {
            viewModel.events.collect { event ->
                when (event) {
                    is TxGroupsListViewModel.TxGroupsListEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        TxGroupsListScreen(
            snackbarController = snackbarController,
            groupsList = groupsList,
            navigateToGroupDetails = {
                navController.navigate(
                    TransactionGroupDetailsScreenSpec.routeWithArg(it)
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}