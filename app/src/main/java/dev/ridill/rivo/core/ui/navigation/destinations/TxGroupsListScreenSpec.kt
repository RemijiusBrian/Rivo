package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.transactionGroups.presentation.groupsList.TxGroupsListScreen
import dev.ridill.rivo.transactionGroups.presentation.groupsList.TxGroupsListViewModel

object TxGroupsListScreenSpec : ScreenSpec {
    override val route: String = "transaction_groups_list"

    override val labelRes: Int = R.string.destination_tx_groups_list

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TxGroupsListViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        TxGroupsListScreen(
            state = state,
            actions = viewModel,
            navigateToGroupDetails = {
                navController.navigate(
                    TransactionGroupDetailsScreenSpec.routeWithArg(it)
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}