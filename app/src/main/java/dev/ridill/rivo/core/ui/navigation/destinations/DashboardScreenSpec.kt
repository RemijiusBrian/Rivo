package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.CollectFlowEffect
import dev.ridill.rivo.core.ui.components.DestinationResultEffect
import dev.ridill.rivo.core.ui.components.OnLifecycleStartEffect
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.dashboard.presentation.DashboardScreen
import dev.ridill.rivo.dashboard.presentation.DashboardViewModel
import dev.ridill.rivo.transactions.presentation.addEditTransaction.ACTION_ADD_EDIT_TX

data object DashboardScreenSpec : ScreenSpec {
    override val route: String = "dashboard"

    override val labelRes: Int = R.string.destination_dashboard

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: DashboardViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        DestinationResultEffect(
            key = ACTION_ADD_EDIT_TX,
            navBackStackEntry = navBackStackEntry,
            context,
            snackbarController,
            onResult = viewModel::onNavResult
        )

        CollectFlowEffect(
            flow = viewModel.events,
            snackbarController,
            context
        ) { event ->
            when (event) {
                DashboardViewModel.DashboardEvent.ScheduleSaved -> {
                    snackbarController.showSnackbar(
                        message = context.getString(R.string.schedule_saved),
                        actionLabel = context.getString(R.string.action_view),
                        onSnackbarResult = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navController.navigate(SchedulesGraphSpec.route)
                            }
                        }
                    )
                }

                is DashboardViewModel.DashboardEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(event.uiText.asString(context))
                }
            }
        }

        OnLifecycleStartEffect {
            viewModel.updateSignedInUsername()
        }

        DashboardScreen(
            snackbarController = snackbarController,
            state = state,
            navigateToAllTransactions = {
                navController.navigate(AllTransactionsScreenSpec.route)
            },
            navigateToAddEditTransaction = {
                navController.navigate(
                    AddEditTransactionScreenSpec.routeWithArg(it)
                )
            },
            navigateToBottomNavDestination = {
                navController.navigate(it.route)
            }
        )
    }
}