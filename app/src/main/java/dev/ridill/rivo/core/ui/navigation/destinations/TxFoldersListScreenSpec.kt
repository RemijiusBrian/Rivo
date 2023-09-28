package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.transactionFolders.presentation.transactionFolderDetails.RESULT_FOLDER_DELETED
import dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList.ACTION_FOLDER_DETAILS
import dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList.TxFoldersListScreen
import dev.ridill.rivo.transactionFolders.presentation.transactionFoldersList.TxFoldersListViewModel

object TxFoldersListScreenSpec : ScreenSpec {
    override val route: String = "transaction_folders_list"

    override val labelRes: Int = R.string.destination_tx_folders_list

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: TxFoldersListViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        DestinationResultEffect(
            key = ACTION_FOLDER_DETAILS,
            navBackStackEntry = navBackStackEntry,
            context,
            snackbarController
        ) {
            when (it) {
                RESULT_FOLDER_DELETED -> R.string.transaction_folder_deleted
                else -> null
            }?.let { resId ->
                snackbarController.showSnackbar(context.getString(resId))
            }
        }

        TxFoldersListScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateToFolderDetails = {
                navController.navigate(
                    TransactionFolderDetailsScreenSpec.routeWithArgs(it)
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}