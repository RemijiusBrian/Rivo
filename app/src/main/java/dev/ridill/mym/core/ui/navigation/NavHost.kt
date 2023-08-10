package dev.ridill.mym.core.ui.navigation

import android.Manifest
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.components.rememberPermissionLauncher
import dev.ridill.mym.core.ui.components.rememberPermissionsState
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.components.simpleFadeIn
import dev.ridill.mym.core.ui.components.simpleFadeOut
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.navigation.destinations.DashboardDestination
import dev.ridill.mym.core.ui.navigation.destinations.SettingsDestination
import dev.ridill.mym.core.ui.navigation.destinations.WelcomeFlowDestination
import dev.ridill.mym.core.ui.util.launchAppNotificationSettings
import dev.ridill.mym.core.ui.util.launchAppSettings
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
        allExpenses(navController)
        addEditExpense(navController)
        settings(navController)
    }
}

// Welcome Flow
private fun NavGraphBuilder.welcomeFlow(navController: NavHostController) {
    composable(
        route = WelcomeFlowDestination.route,
        enterTransition = { simpleFadeIn() },
        exitTransition = { simpleFadeOut() }
    ) { navBackStackEntry ->
        val viewModel: WelcomeFlowViewModel = hiltViewModel(navBackStackEntry)
        val flowStop by viewModel.currentFlowStop.collectAsStateWithLifecycle()
        val incomeInput = viewModel.incomeInput.collectAsStateWithLifecycle()
        val showPermissionRationale by viewModel.showNotificationRationale
            .collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val permissionsState = if (BuildUtil.isNotificationRuntimePermissionNeeded())
            rememberPermissionsState(
                permissionString = Manifest.permission.POST_NOTIFICATIONS,
                launcher = rememberPermissionLauncher(
                    onResult = { viewModel.onPermissionResponse() }
                )
            )
        else null

        LaunchedEffect(viewModel, snackbarController, context) {
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
                        snackbarController.showSnackbar(
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
            snackbarController = snackbarController,
            flowStop = flowStop,
            incomeInput = { incomeInput.value },
            showPermissionRationale = showPermissionRationale,
            actions = viewModel
        )
    }
}

// Dashboard
private fun NavGraphBuilder.dashboard(navController: NavHostController) {
    composable(
        route = DashboardDestination.route,
        exitTransition = { simpleFadeOut() },
        popEnterTransition = { simpleFadeIn() }
    ) { navBackStackEntry ->
        val viewModel: DashboardViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val dashboardResult = navBackStackEntry
            .savedStateHandle
            .get<String>(DASHBOARD_ACTION_RESULT)

        LaunchedEffect(dashboardResult, context, snackbarController) {
            when (dashboardResult) {
                RESULT_EXPENSE_ADDED -> R.string.expense_added
                RESULT_EXPENSE_UPDATED -> R.string.expense_updated
                RESULT_EXPENSE_DELETED -> R.string.expense_deleted
                else -> null
            }?.let { messageRes ->
                snackbarController.showSnackbar(context.getString(messageRes))
            }
            navBackStackEntry.savedStateHandle
                .remove<String>(DASHBOARD_ACTION_RESULT)
        }

        DashboardScreen(
            snackbarController = snackbarController,
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
        deepLinks = AddEditExpenseDestination.deepLinks,
        enterTransition = { slideInVertically { it } },
        popExitTransition = { slideOutVertically { it } },
    ) { navBackStackEntry ->
        val viewModel: AddEditExpenseViewModel = hiltViewModel(navBackStackEntry)
        val amount = viewModel.amountInput.collectAsStateWithLifecycle(initialValue = "")
        val note = viewModel.noteInput.collectAsStateWithLifecycle(initialValue = "")
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagNameInput = viewModel.tagNameInput.collectAsStateWithLifecycle()
        val tagColorInput = viewModel.tagColorInput.collectAsStateWithLifecycle()

        val isEditMode = AddEditExpenseDestination.isArgEditMode(navBackStackEntry)

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        LaunchedEffect(viewModel, snackbarController, context) {
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
                        snackbarController.showSnackbar(
                            message = event.uiText.asString(context),
                            isError = event.uiText.isErrorText
                        )
                    }
                }
            }
        }

        AddEditExpenseScreen(
            snackbarController = snackbarController,
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

// All Expenses
private fun NavGraphBuilder.allExpenses(navController: NavHostController) {
    composable(
        route = AllExpensesDestination.route,
        enterTransition = { simpleFadeIn() },
        popExitTransition = { simpleFadeOut() }
    ) { navBackStackEntry ->
        val viewModel: AllExpensesViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()
        val tagNameInput = viewModel.tagNameInput.collectAsStateWithLifecycle()
        val tagColorInput = viewModel.tagColorInput.collectAsStateWithLifecycle()

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
            tagNameInput = { tagNameInput.value },
            tagColorInput = { tagColorInput.value },
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}

// Settings
private fun NavGraphBuilder.settings(navController: NavHostController) {
    composable(
        route = SettingsDestination.route,
        enterTransition = { simpleFadeIn() },
        popExitTransition = { simpleFadeOut() }
    ) { navBackStackEntry ->
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        val smsPermissionState =
            rememberPermissionsState(permissionString = Manifest.permission.RECEIVE_SMS)

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    is SettingsViewModel.SettingsEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    SettingsViewModel.SettingsEvent.RequestSmsPermission -> {
                        if (smsPermissionState.isPermanentlyDenied) {
                            context.launchAppSettings()
                        } else {
                            smsPermissionState.launchRequest()
                        }
                    }
                }
            }
        }

        SettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToNotificationSettings = context::launchAppNotificationSettings,
            navigateToSourceCode = {
                context.launchUrlExternally(BuildConfig.GITHUB_REPO_URL)
            }
        )
    }
}