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
import dev.ridill.rivo.core.domain.util.orFalse
import dev.ridill.rivo.core.ui.components.NavigationResultEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.tags.presentation.tagSelection.TagSelectionSheet
import dev.ridill.rivo.tags.presentation.tagSelection.TagSelectionViewModel
import java.util.Currency

data object TagSelectionSheetSpec : BottomSheetSpec {

    override val route: String = "tag_selection/{$ARG_MULTI_SELECTION}/{$ARG_PRE_SELECTED_ID}"
    override val labelRes: Int = R.string.destination_tag_selection

    override val arguments: List<NamedNavArgument> = listOf(
        navArgument(ARG_MULTI_SELECTION) {
            type = NavType.BoolType
            nullable = false
            defaultValue = false
        },
        navArgument(ARG_PRE_SELECTED_ID) {
            type = NavType.LongType
            nullable = false
            defaultValue = NavDestination.ARG_INVALID_ID_LONG
        }
    )

    fun routeWithArgs(
        multiSelection: Boolean,
        preselectedId: Long? = null
    ) = route
        .replace(
            oldValue = "{$ARG_MULTI_SELECTION}",
            newValue = multiSelection.toString()
        )
        .replace(
            oldValue = "{$ARG_PRE_SELECTED_ID}",
            newValue = (preselectedId ?: NavDestination.ARG_INVALID_ID_LONG).toString()
        )

    fun getMultiSelectionFromSavedStateHandle(savedStateHandle: SavedStateHandle): Boolean =
        savedStateHandle.get<Boolean>(ARG_MULTI_SELECTION).orFalse()

    private fun getMultiSelectionArg(navBackStackEntry: NavBackStackEntry): Boolean =
        navBackStackEntry.arguments?.getBoolean(ARG_MULTI_SELECTION).orFalse()

    fun getPreselectedIdFromSavedStateHandle(savedStateHandle: SavedStateHandle): Long? =
        savedStateHandle.get<Long>(ARG_PRE_SELECTED_ID)
            ?.takeIf { it > NavDestination.ARG_INVALID_ID_LONG }

    const val SELECTED_IDS = "SELECTED_IDS"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry,
        appCurrencyPreference: Currency
    ) {
        val viewModel: TagSelectionViewModel = hiltViewModel(navBackStackEntry)
        val multiSelection = getMultiSelectionArg(navBackStackEntry)
        val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
        val selectedIds by viewModel.selectedIds.collectAsStateWithLifecycle()
        val tagsLazyPagingItems = viewModel.tagsPagingData.collectAsLazyPagingItems()

        NavigationResultEffect<Long>(
            key = AddEditTagSheetSpec.CRATED_TAG_ID,
            navBackStackEntry = navBackStackEntry,
            viewModel
        ) { it?.let(viewModel::onItemClick) }

        TagSelectionSheet(
            multiSelection = multiSelection,
            tagsLazyPagingItems = tagsLazyPagingItems,
            searchQuery = { searchQuery.value },
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onDismiss = navController::navigateUp,
            selectedIds = selectedIds,
            onItemClick = viewModel::onItemClick,
            onConfirm = {
                navController.navigateUpWithResult(
                    key = SELECTED_IDS,
                    result = selectedIds
                )
            },
            navigateToAddEditTag = {
                navController.navigate(AddEditTagSheetSpec.routeWithArg())
            }
        )
    }
}

private const val ARG_MULTI_SELECTION = "ARG_MULTI_SELECTION"
private const val ARG_PRE_SELECTED_ID = "ARG_PRE_SELECTED_ID"