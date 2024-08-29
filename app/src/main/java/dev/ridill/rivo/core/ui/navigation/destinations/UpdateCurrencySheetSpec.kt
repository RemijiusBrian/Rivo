package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.settings.presentation.currencyUpdate.UpdateCurrencySheet
import dev.ridill.rivo.settings.presentation.currencyUpdate.UpdateCurrencyViewModel

data object UpdateCurrencySheetSpec : BottomSheetSpec {
    override val route: String = "update_currency"

    override val labelRes: Int = R.string.destination_update_currency

    const val UPDATE_CURRENCY_RESULT = "UPDATE_CURRENCY_RESULT"
    const val RESULT_CURRENCY_UPDATED = "RESULT_CURRENCY_UPDATED"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: UpdateCurrencyViewModel = hiltViewModel(navBackStackEntry)
        val searchQuery = viewModel.searchQuery.collectAsStateWithLifecycle()
        val currenciesLazyPagingItems = viewModel.currencyPagingData.collectAsLazyPagingItems()

        CollectFlowEffect(flow = viewModel.events) { event ->
            when (event) {
                UpdateCurrencyViewModel.UpdateCurrencyEvent.CurrencyUpdated -> {
                    navController.navigateUpWithResult(
                        UPDATE_CURRENCY_RESULT,
                        RESULT_CURRENCY_UPDATED
                    )
                }
            }
        }

        UpdateCurrencySheet(
            searchQuery = { searchQuery.value },
            currenciesPagingData = currenciesLazyPagingItems,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onDismiss = navController::popBackStack,
            onConfirm = viewModel::onConfirm
        )
    }
}