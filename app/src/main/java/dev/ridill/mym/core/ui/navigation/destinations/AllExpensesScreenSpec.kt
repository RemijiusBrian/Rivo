package dev.ridill.mym.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesScreen
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesViewModel

object AllExpensesScreenSpec : ScreenSpec, BottomNavDestination {
    override val iconRes: Int = R.drawable.ic_all_expenses

    override val route: String = "all_expenses"

    override val labelRes: Int = R.string.destination_all_expenses

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: AllExpensesViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagInput = viewModel.tagInput.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val hapticFeedback = LocalHapticFeedback.current

        LaunchedEffect(viewModel, context, snackbarController) {
            viewModel.events.collect { event ->
                when (event) {
                    is AllExpensesViewModel.AllExpenseEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    is AllExpensesViewModel.AllExpenseEvent.ProvideHapticFeedback -> {
                        hapticFeedback.performHapticFeedback(event.type)
                    }
                }
            }
        }

        AllExpensesScreen(
            snackbarController = snackbarController,
            state = state,
            tagNameInput = { tagInput.value?.name.orEmpty() },
            tagColorInput = { tagInput.value?.colorCode },
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}