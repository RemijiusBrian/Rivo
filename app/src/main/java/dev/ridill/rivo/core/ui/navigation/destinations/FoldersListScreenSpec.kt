package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.folders.presentation.folderDetails.RESULT_FOLDER_DELETED
import dev.ridill.rivo.folders.presentation.foldersList.ACTION_FOLDER_DETAILS
import dev.ridill.rivo.folders.presentation.foldersList.FoldersListScreen
import dev.ridill.rivo.folders.presentation.foldersList.FoldersListViewModel

object FoldersListScreenSpec : ScreenSpec {
    override val route: String = "folders_list"

    override val labelRes: Int = R.string.destination_folders_list

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: FoldersListViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val foldersList = viewModel.folderListPagingData.collectAsLazyPagingItems()

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

        FoldersListScreen(
            snackbarController = snackbarController,
            foldersList = foldersList,
            state = state,
            actions = viewModel,
            navigateToFolderDetails = {
                navController.navigate(
                    FolderDetailsScreenSpec.routeWithArgs(it)
                )
            },
            navigateUp = navController::navigateUp
        )
    }
}