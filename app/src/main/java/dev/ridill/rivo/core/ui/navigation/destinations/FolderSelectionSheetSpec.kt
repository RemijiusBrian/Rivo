package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.folders.presentation.components.FolderListSearchSheet
import dev.ridill.rivo.folders.presentation.folderSelection.FolderSelectionViewModel
import java.util.Currency

data object FolderSelectionSheetSpec : BottomSheetSpec {
    override val route: String = "folder_selection_sheet"
    override val labelRes: Int = R.string.destination_folder_selection_sheet

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: FolderSelectionViewModel = hiltViewModel(navBackStackEntry)
        val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
        val foldersList = viewModel.folderListPaged.collectAsLazyPagingItems()

        FolderListSearchSheet(
            searchQuery = { searchQuery.value },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            foldersListLazyPagingItems = foldersList,
            onFolderClick = {
                navController.navigateUpWithResult(key = SELECTED_FOLDER_ID, result = it.id)
            },
            onCreateNewClick = {
                navController.navigate(
                    FolderDetailsScreenSpec.routeWithArgs(
                        transactionFolderId = null,
                        exitAfterClear = true
                    )
                )
            },
            onDismiss = navController::navigateUp
        )
    }
}

const val SELECTED_FOLDER_ID = "SELECTED_FOLDER_ID"