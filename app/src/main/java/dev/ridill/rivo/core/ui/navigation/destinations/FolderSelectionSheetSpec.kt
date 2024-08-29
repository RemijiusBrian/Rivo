package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.NavigationResultEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.folders.presentation.folderSelection.FolderSelectionSheet
import dev.ridill.rivo.folders.presentation.folderSelection.FolderSelectionViewModel

data object FolderSelectionSheetSpec : BottomSheetSpec {
    override val route: String = "folder_selection_sheet/{$ARG_PRE_SELECTED_ID}"
    override val labelRes: Int = R.string.destination_folder_selection_sheet

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_PRE_SELECTED_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = NavDestination.ARG_INVALID_ID_LONG
        }
    )

    fun routeWithArgs(preselectedId: Long?): String = route
        .replace(
            oldValue = "{$ARG_PRE_SELECTED_ID}",
            newValue = (preselectedId ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )

    const val SELECTED_FOLDER_ID = "SELECTED_FOLDER_ID"

    fun getPreselectedIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<Long>(ARG_PRE_SELECTED_ID)
            ?.takeIf { it > NavDestination.ARG_INVALID_ID_LONG }

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: FolderSelectionViewModel = hiltViewModel(navBackStackEntry)
        val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
        val foldersList = viewModel.folderListPaged.collectAsLazyPagingItems()
        val selectedId by viewModel.selectedFolderId.collectAsStateWithLifecycle()

        NavigationResultEffect(
            key = AddEditFolderSheetSpec.ACTION_FOLDER_SAVED,
            navBackStackEntry = navBackStackEntry,
            keys = arrayOf(viewModel),
            onResult = viewModel::onFolderSelect
        )

        FolderSelectionSheet(
            searchQuery = { searchQuery.value },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            foldersListLazyPagingItems = foldersList,
            onFolderSelect = viewModel::onFolderSelect,
            onCreateNewClick = {
                navController.navigate(
                    AddEditFolderSheetSpec.routeWithArg()
                )
            },
            onDismiss = navController::navigateUp,
            onClearSelectionClick = {
                navController.navigateUpWithResult(
                    key = SELECTED_FOLDER_ID,
                    result = NavDestination.ARG_INVALID_ID_LONG
                )
            },
            onConfirm = {
                navController.navigateUpWithResult(
                    key = SELECTED_FOLDER_ID,
                    result = selectedId
                )
            },
            selectedId = selectedId
        )
    }
}

private const val ARG_PRE_SELECTED_ID = "ARG_PRE_SELECTED_ID"