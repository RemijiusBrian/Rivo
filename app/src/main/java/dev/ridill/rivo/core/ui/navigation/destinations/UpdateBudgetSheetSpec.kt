package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.domain.util.Zero
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.navigateUpWithResult
import dev.ridill.rivo.core.ui.util.TextFormat
import dev.ridill.rivo.settings.presentation.budgetUpdate.UpdateBudgetSheet
import dev.ridill.rivo.settings.presentation.budgetUpdate.UpdateBudgetViewModel

data object UpdateBudgetSheetSpec : BottomSheetSpec {

    override val route: String
        get() = "update_budget"

    override val labelRes: Int
        get() = R.string.destination_update_budget

    const val UPDATE_BUDGET_RESULT = "UPDATE_BUDGET_RESULT"
    const val RESULT_BUDGET_UPDATED = "RESULT_BUDGET_UPDATED"

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: UpdateBudgetViewModel = hiltViewModel(navBackStackEntry)
        val currentBudget by viewModel.currentBudget.collectAsStateWithLifecycle(Long.Zero)
        val input = viewModel.budgetInput.collectAsStateWithLifecycle()
        val inputError by viewModel.budgetInputError.collectAsStateWithLifecycle()

        CollectFlowEffect(viewModel.events) { event ->
            when (event) {
                UpdateBudgetViewModel.UpdateBudgetEvent.BudgetUpdated -> {
                    navController.navigateUpWithResult(
                        key = UPDATE_BUDGET_RESULT,
                        result = RESULT_BUDGET_UPDATED
                    )
                }
            }
        }

        UpdateBudgetSheet(
            placeholder = TextFormat.number(currentBudget),
            budgetInput = { input.value },
            onInputChange = viewModel::onInputChange,
            onConfirm = viewModel::onConfirm,
            onDismiss = navController::navigateUp,
            errorMessage = inputError
        )
    }
}