package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.dashboard.presentation.DASHBOARD_ACTION_RESULT
import dev.ridill.rivo.dashboard.presentation.DashboardScreen
import dev.ridill.rivo.dashboard.presentation.DashboardViewModel
import dev.ridill.rivo.expense.presentation.addEditExpense.RESULT_EXPENSE_ADDED
import dev.ridill.rivo.expense.presentation.addEditExpense.RESULT_EXPENSE_DELETED
import dev.ridill.rivo.expense.presentation.addEditExpense.RESULT_EXPENSE_UPDATED

object DashboardScreenSpec : ScreenSpec {
    override val route: String = "dashboard"

    override val labelRes: Int = R.string.destination_dashboard

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: DashboardViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        DestinationResultEffect(
            key = DASHBOARD_ACTION_RESULT,
            navBackStackEntry = navBackStackEntry,
            context,
            snackbarController,
        ) {
            when (it) {
                RESULT_EXPENSE_ADDED -> R.string.expense_added
                RESULT_EXPENSE_UPDATED -> R.string.expense_updated
                RESULT_EXPENSE_DELETED -> R.string.expense_deleted
                else -> null
            }?.let { messageRes ->
                snackbarController.showSnackbar(context.getString(messageRes))
            }
        }

        DashboardScreen(
            snackbarController = snackbarController,
            state = state,
            navigateToAllExpenses = {
                navController.navigate(AllExpensesScreenSpec.route)
            },
            navigateToAddEditExpense = {
                navController.navigate(AddEditExpenseScreenSpec.routeWithArg(it))
            },
            navigateToBottomNavDestination = {
                navController.navigate(it.route)
            }
        )
    }
}