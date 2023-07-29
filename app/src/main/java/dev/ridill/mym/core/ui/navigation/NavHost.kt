package dev.ridill.mym.core.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.navigation.destinations.DashboardDestination
import dev.ridill.mym.dashboard.presentation.DASHBOARD_ACTION_RESULT
import dev.ridill.mym.dashboard.presentation.DashboardScreen
import dev.ridill.mym.dashboard.presentation.DashboardViewModel
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseScreen
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_ADDED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_DELETED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_UPDATED

@Composable
fun MYMNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = DashboardDestination.route,
        modifier = modifier
    ) {
        dashboard(navController)
        addEditExpense(navController)
    }
}

// Dashboard
private fun NavGraphBuilder.dashboard(navController: NavHostController) {
    composable(
        route = DashboardDestination.route,
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() }
    ) { navBackStackEntry ->
        val viewModel: DashboardViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val dashboardResult = navBackStackEntry
            .savedStateHandle
            .get<String>(DASHBOARD_ACTION_RESULT)

        LaunchedEffect(dashboardResult, context) {
            navBackStackEntry.savedStateHandle
                .remove<String>(DASHBOARD_ACTION_RESULT)

            when (dashboardResult) {
                RESULT_EXPENSE_ADDED -> R.string.expense_added
                RESULT_EXPENSE_UPDATED -> R.string.expense_updated
                RESULT_EXPENSE_DELETED -> R.string.expense_deleted
                else -> null
            }?.let { messageRes ->
                // TODO: Show Snackbar
            }
        }

        DashboardScreen(
            state = state,
            navigateToAddEditExpense = {
                navController.navigate(AddEditExpenseDestination.routeWithArg(it))
            }
        )
    }
}

// Add/Edit Expense
private fun NavGraphBuilder.addEditExpense(navController: NavHostController) {
    composable(
        route = AddEditExpenseDestination.route,
        arguments = AddEditExpenseDestination.arguments,
        enterTransition = { slideInVertically { it } },
        popExitTransition = { slideOutVertically { it } }
    ) { navBackStackEntry ->
        val viewModel: AddEditExpenseViewModel = hiltViewModel(navBackStackEntry)
        val amount by viewModel.amount.collectAsStateWithLifecycle(initialValue = "")
        val note by viewModel.note.collectAsStateWithLifecycle(initialValue = "")

        val isEditMode = AddEditExpenseDestination.isArgEditMode(navBackStackEntry)

        LaunchedEffect(viewModel) {
            viewModel.events.collect { event ->
                when (event) {
                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseAdded -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(DASHBOARD_ACTION_RESULT, RESULT_EXPENSE_ADDED)
                        navController.popBackStack()
                    }

                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseDeleted -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(DASHBOARD_ACTION_RESULT, RESULT_EXPENSE_DELETED)
                        navController.popBackStack()
                    }

                    AddEditExpenseViewModel.AddEditExpenseEvent.ExpenseUpdated -> {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(DASHBOARD_ACTION_RESULT, RESULT_EXPENSE_UPDATED)
                        navController.popBackStack()
                    }
                }
            }
        }

        AddEditExpenseScreen(
            amountInput = amount,
            noteInput = note,
            isEditMode = isEditMode,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}