package dev.ridill.mym.core.ui.navigation

import android.Manifest
import android.os.Build
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
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.defaultFadeIn
import dev.ridill.mym.core.ui.components.defaultFadeOut
import dev.ridill.mym.core.ui.components.rememberPermissionLauncher
import dev.ridill.mym.core.ui.components.rememberPermissionsState
import dev.ridill.mym.core.ui.components.rememberSnackbarHostState
import dev.ridill.mym.core.ui.components.showMymSnackbar
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.navigation.destinations.DashboardDestination
import dev.ridill.mym.core.ui.navigation.destinations.SettingsDestination
import dev.ridill.mym.core.ui.navigation.destinations.WelcomeFlowDestination
import dev.ridill.mym.core.ui.util.launchNotificationSettings
import dev.ridill.mym.core.ui.util.launchUrlExternally
import dev.ridill.mym.dashboard.presentation.DASHBOARD_ACTION_RESULT
import dev.ridill.mym.dashboard.presentation.DashboardScreen
import dev.ridill.mym.dashboard.presentation.DashboardViewModel
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseScreen
import dev.ridill.mym.expense.presentation.addEditExpense.AddEditExpenseViewModel
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_ADDED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_DELETED
import dev.ridill.mym.expense.presentation.addEditExpense.RESULT_EXPENSE_UPDATED
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesScreen
import dev.ridill.mym.expense.presentation.allExpenses.AllExpensesViewModel
import dev.ridill.mym.settings.presentation.settings.SettingsScreen
import dev.ridill.mym.settings.presentation.settings.SettingsViewModel
import dev.ridill.mym.welcomeFlow.presentation.WelcomeFlowScreen
import dev.ridill.mym.welcomeFlow.presentation.WelcomeFlowViewModel

@Composable
fun MYMNavHost(
    navController: NavHostController,
    showWelcomeFlow: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (showWelcomeFlow) WelcomeFlowDestination.route
        else DashboardDestination.route,
        modifier = modifier
    ) {
        welcomeFlow(navController)
        dashboard(navController)
        addEditExpense(navController)
        settings(navController)
        allExpenses(navController)
    }
}

// Welcome Flow
private fun NavGraphBuilder.welcomeFlow(navController: NavHostController) {
    composable(WelcomeFlowDestination.route) { navBackStackEntry ->
        val viewModel: WelcomeFlowViewModel = hiltViewModel(navBackStackEntry)
        val flowStop by viewModel.currentFlowStop.collectAsStateWithLifecycle()
        val limitInput = viewModel.limitInput.collectAsStateWithLifecycle()
        val showPermissionRationale by viewModel.showNotificationRationale
            .collectAsStateWithLifecycle()

        val snackbarHostState = rememberSnackbarHostState()
        val context = LocalContext.current
        val permissionsState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            rememberPermissionsState(
                permissionString = Manifest.permission.POST_NOTIFICATIONS,
                launcher = rememberPermissionLauncher(
                    onResult = { viewModel.onPermissionResponse() }
                )
            )
        else null

        LaunchedEffect(viewModel, snackbarHostState, context) {
            viewModel.events.collect { event ->
                when (event) {
                    WelcomeFlowViewModel.WelcomeFlowEvent.RequestPermissionRequest -> {
                        if (permissionsState != null) {
                            permissionsState.launchRequest()
                        } else {
                            viewModel.onPermissionResponse()
                        }
                    }

                    is WelcomeFlowViewModel.WelcomeFlowEvent.ShowUiMessage -> {
                        snackbarHostState.showMymSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    WelcomeFlowViewModel.WelcomeFlowEvent.WelcomeFlowConcluded -> {
                        navController.navigate(DashboardDestination.route)
                    }
                }
            }
        }

        WelcomeFlowScreen(
            snackbarHostState = snackbarHostState,
            flowStop = flowStop,
            limitInput = { limitInput.value },
            showPermissionRationale = showPermissionRationale,
            actions = viewModel
        )
    }
}

// Dashboard
private fun NavGraphBuilder.dashboard(navController: NavHostController) {
    composable(
        route = DashboardDestination.route,
        exitTransition = { defaultFadeOut() },
        popEnterTransition = { defaultFadeIn() }
    ) { navBackStackEntry ->
        val viewModel: DashboardViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarHostState = rememberSnackbarHostState()
        val context = LocalContext.current
        val dashboardResult = navBackStackEntry
            .savedStateHandle
            .get<String>(DASHBOARD_ACTION_RESULT)

        LaunchedEffect(dashboardResult, context, snackbarHostState) {
            when (dashboardResult) {
                RESULT_EXPENSE_ADDED -> R.string.expense_added
                RESULT_EXPENSE_UPDATED -> R.string.expense_updated
                RESULT_EXPENSE_DELETED -> R.string.expense_deleted
                else -> null
            }?.let { messageRes ->
                snackbarHostState.showMymSnackbar(context.getString(messageRes))
            }
            navBackStackEntry.savedStateHandle
                .remove<String>(DASHBOARD_ACTION_RESULT)
        }

        DashboardScreen(
            snackbarHostState = snackbarHostState,
            state = state,
            navigateToAddEditExpense = {
                navController.navigate(AddEditExpenseDestination.routeWithArg(it))
            },
            navigateToBottomNavDestination = {
                navController.navigate(it.route)
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
        val amount = viewModel.amountInput.collectAsStateWithLifecycle(initialValue = "")
        val note = viewModel.noteInput.collectAsStateWithLifecycle(initialValue = "")
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagNameInput = viewModel.tagNameInput.collectAsStateWithLifecycle()
        val tagColorInput = viewModel.tagColorInput.collectAsStateWithLifecycle()

        val isEditMode = AddEditExpenseDestination.isArgEditMode(navBackStackEntry)

        val snackbarHostState = rememberSnackbarHostState()
        val context = LocalContext.current

        LaunchedEffect(viewModel, snackbarHostState, context) {
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

                    is AddEditExpenseViewModel.AddEditExpenseEvent.ShowUiMessage -> {
                        snackbarHostState.showMymSnackbar(
                            message = event.uiText.asString(context),
                            isError = event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        AddEditExpenseScreen(
            snackbarHostState = snackbarHostState,
            amountInput = { amount.value },
            noteInput = { note.value },
            isEditMode = isEditMode,
            tagNameInput = { tagNameInput.value },
            tagColorInput = { tagColorInput.value },
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

private fun NavGraphBuilder.settings(navController: NavHostController) {
    composable(
        route = SettingsDestination.route,
        enterTransition = { defaultFadeIn() },
        popExitTransition = { defaultFadeOut() }
    ) { navBackStackEntry ->
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarHostState = rememberSnackbarHostState()
        val context = LocalContext.current

        LaunchedEffect(viewModel, snackbarHostState, context) {
            viewModel.events.collect { event ->
                when (event) {
                    is SettingsViewModel.SettingsEvent.ShowUiMessage -> {
                        snackbarHostState.showMymSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        SettingsScreen(
            snackbarHostState = snackbarHostState,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToNotificationSettings = context::launchNotificationSettings,
            navigateToSourceCode = {
                context.launchUrlExternally(BuildConfig.GITHUB_REPO_URL)
            }
        )
    }
}

// All Expenses
private fun NavGraphBuilder.allExpenses(navController: NavHostController) {
    composable(
        route = AllExpensesDestination.route,
        enterTransition = { defaultFadeIn() },
        popExitTransition = { defaultFadeOut() }
    ) { navBackStackEntry ->
        val viewModel: AllExpensesViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagNameInput = viewModel.tagNameInput.collectAsStateWithLifecycle()
        val tagColorInput = viewModel.tagColorInput.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarHostState = rememberSnackbarHostState()

        LaunchedEffect(viewModel, context, snackbarHostState) {
            viewModel.events.collect { event ->
                when (event) {
                    is AllExpensesViewModel.AllExpenseEvent.ShowUiMessage -> {
                        snackbarHostState.showMymSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        AllExpensesScreen(
            snackbarHostState = snackbarHostState,
            state = state,
            tagNameInput = { tagNameInput.value },
            tagColorInput = { tagColorInput.value },
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}