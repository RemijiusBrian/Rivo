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
import dev.ridill.rivo.tags.presentation.addEditTag.AddEditTagSheet
import dev.ridill.rivo.tags.presentation.addEditTag.AddEditTagViewModel

data object AddEditTagSheetSpec : BottomSheetSpec {
    override val labelRes: Int = R.string.destination_add_edit_transaction
    override val route: String = "add_edit_tag/{$ARG_TAG_ID}"

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_TAG_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = NavDestination.ARG_INVALID_ID_LONG
        },
    )

    fun routeWithArg(tagId: Long? = null): String = route
        .replace(
            oldValue = "{$ARG_TAG_ID}",
            newValue = (tagId ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )

    fun getTagIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long =
        savedStateHandle.get<Long>(ARG_TAG_ID) ?: NavDestination.ARG_INVALID_ID_LONG

    private fun isArgEditMode(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getLong(ARG_TAG_ID) != NavDestination.ARG_INVALID_ID_LONG

    const val SAVED_TAG_ID = "SAVED_TAG_ID"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: AddEditTagViewModel = hiltViewModel(navBackStackEntry)
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val input = viewModel.tagInput.collectAsStateWithLifecycle()
        val error by viewModel.tagInputError.collectAsStateWithLifecycle()
        val showDeleteTagConfirmation by viewModel.showTagDeleteConfirmation.collectAsStateWithLifecycle()

        val isEditMode = isArgEditMode(navBackStackEntry)

        CollectFlowEffect(
            flow = viewModel.events
        ) { event ->
            when (event) {
                is AddEditTagViewModel.AddEditTagEvent.TagSaved -> {
                    navController.navigateUpWithResult(
                        SAVED_TAG_ID,
                        event.tagId
                    )
                }

                AddEditTagViewModel.AddEditTagEvent.TagDeleted -> {
                    navController.navigateUp()
                }
            }
        }

        AddEditTagSheet(
            isLoading = isLoading,
            name = { input.value.name },
            selectedColorCode = { input.value.colorCode },
            excluded = { input.value.excluded },
            errorMessage = error,
            isEditMode = isEditMode,
            onDismiss = navController::navigateUp,
            showDeleteTagConfirmation = showDeleteTagConfirmation,
            actions = viewModel
        )
    }
}

private const val ARG_TAG_ID = "ARG_TAG_ID"