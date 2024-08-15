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
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.folders.presentation.addEditFolder.AddEditFolderSheet
import dev.ridill.rivo.folders.presentation.addEditFolder.AddEditFolderViewModel
import java.util.Currency

data object AddEditFolderSheetSpec : BottomSheetSpec {

    override val route: String = "add_edit_folder/{$ARG_FOLDER_ID}"
    override val labelRes: Int = R.string.destination_add_edit_folder

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_FOLDER_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = NavDestination.ARG_INVALID_ID_LONG
        },
    )

    fun routeWithArg(id: Long? = null): String = route
        .replace(
            oldValue = "{$ARG_FOLDER_ID}",
            newValue = (id ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )

    fun getFolderIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_FOLDER_ID) ?: NavDestination.ARG_INVALID_ID_LONG

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_FOLDER_ID) != NavDestination.ARG_INVALID_ID_LONG

    const val ACTION_FOLDER_SAVED = "ACTION_FOLDER_SAVED"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: AddEditFolderViewModel = hiltViewModel(navBackStackEntry)
        val input = viewModel.folderInput.collectAsStateWithLifecycle()
        val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

        val editMode = isArgEditMode(navBackStackEntry)

        CollectFlowEffect(
            flow = viewModel.events
        ) { event ->
            when (event) {
                is AddEditFolderViewModel.AddEditFolderEvent.FolderSaved -> {
                    navController.navigateUpWithResult(
                        ACTION_FOLDER_SAVED,
                        event.tagId
                    )
                }
            }
        }

        AddEditFolderSheet(
            isLoading = isLoading,
            name = { input.value.name },
            excluded = { input.value.excluded },
            errorMessage = errorMessage,
            isEditMode = editMode,
            actions = viewModel,
            onDismiss = navController::navigateUp
        )
    }
}

private const val ARG_FOLDER_ID = "ARG_FOLDER_ID"