package dev.ridill.mym.core.ui.navigation

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.navigation.navigation
import dev.ridill.mym.BuildConfig
import dev.ridill.mym.R
import dev.ridill.mym.core.domain.util.BuildUtil
import dev.ridill.mym.core.ui.components.rememberMultiplePermissionsLauncher
import dev.ridill.mym.core.ui.components.rememberMultiplePermissionsState
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.components.simpleFadeIn
import dev.ridill.mym.core.ui.components.simpleFadeOut
import dev.ridill.mym.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.mym.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.mym.core.ui.navigation.destinations.AddEditExpenseDestination
import dev.ridill.mym.core.ui.navigation.destinations.AllExpensesDestination
import dev.ridill.mym.core.ui.navigation.destinations.BackupSettingsDestination
import dev.ridill.mym.core.ui.navigation.destinations.DashboardDestination
import dev.ridill.mym.core.ui.navigation.destinations.SettingsDestination
import dev.ridill.mym.core.ui.navigation.destinations.SettingsGraph
import dev.ridill.mym.core.ui.navigation.destinations.WelcomeFlowDestination
import dev.ridill.mym.core.ui.util.launchAppNotificationSettings
import dev.ridill.mym.core.ui.util.launchAppSettings
import dev.ridill.mym.core.ui.util.launchUrlExternally
import dev.ridill.mym.core.ui.util.restartApplication
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
import dev.ridill.mym.settings.presentation.backupSettings.BackupSettingsScreen
import dev.ridill.mym.settings.presentation.backupSettings.BackupSettingsViewModel
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
        settingsGraph(navController)
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
        val budgetInput = viewModel.budgetInput.collectAsStateWithLifecycle()
        val availableBackup by viewModel.availableBackup.collectAsStateWithLifecycle()
        val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current
        val permissionsList = if (BuildUtil.isNotificationRuntimePermissionNeeded()) listOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.RECEIVE_SMS
        ) else listOf(Manifest.permission.RECEIVE_SMS)
        val permissionsLauncher = rememberMultiplePermissionsLauncher(
            onResult = { viewModel.onPermissionResponse() }
        )
        val multiplePermissionsState = rememberMultiplePermissionsState(
            permissions = permissionsList,
            launcher = permissionsLauncher
        )

        val signInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    viewModel.onSignInResult(result.data)
                }
            }
        )

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    WelcomeFlowViewModel.WelcomeFlowEvent.LaunchPermissionRequests -> {
                        multiplePermissionsState.launchRequest()
                    }

                    is WelcomeFlowViewModel.WelcomeFlowEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    WelcomeFlowViewModel.WelcomeFlowEvent.WelcomeFlowConcluded -> {
                        navController.navigate(DashboardDestination.route) {
                            popUpTo(WelcomeFlowDestination.route) {
                                inclusive = true
                            }
                        }
                    }

                    is WelcomeFlowViewModel.WelcomeFlowEvent.LaunchGoogleSignIn -> {
                        signInLauncher.launch(event.intent)
                    }

                    WelcomeFlowViewModel.WelcomeFlowEvent.RestartApplication -> {
                        context.restartApplication()
                    }
                }
            }
        }

        WelcomeFlowScreen(
            snackbarController = snackbarController,
            flowStop = flowStop,
            availableBackup = availableBackup,
            budgetInput = { budgetInput.value },
            restoreState = restoreState,
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

// Settings Graph
private fun NavGraphBuilder.settingsGraph(navController: NavHostController) {
    navigation(
        startDestination = SettingsDestination.route,
        route = SettingsGraph.route
    ) {
        settings(navController)
        backupSettings(navController)
    }
}

// Settings
private fun NavGraphBuilder.settings(navController: NavHostController) {
    composable(
        route = SettingsDestination.route,
        enterTransition = { simpleFadeIn() },
        popExitTransition = { simpleFadeOut() },
        exitTransition = { slideOutHorizontallyWithFadeOut { -it } },
        popEnterTransition = { slideInHorizontallyWithFadeIn { -it } }
    ) { navBackStackEntry ->
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    is SettingsViewModel.SettingsEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    SettingsViewModel.SettingsEvent.NavigateToAppPermissionSettings -> {
                        context.launchAppSettings()
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
            viewSourceCodeInBrowser = {
                context.launchUrlExternally(BuildConfig.GITHUB_REPO_URL)
            },
            navigateToBackupSettings = { navController.navigate(BackupSettingsDestination.route) }
        )
    }
}

// Backup Settings
private fun NavGraphBuilder.backupSettings(navController: NavHostController) {
    composable(
        route = BackupSettingsDestination.route,
        enterTransition = { slideInHorizontallyWithFadeIn { it } },
        popExitTransition = { slideOutHorizontallyWithFadeOut { it } }
    ) { navBackStackEntry ->
        val viewModel: BackupSettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val snackbarController = rememberSnackbarController()
        val context = LocalContext.current

        val googleSignInLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult(),
            onResult = viewModel::onSignInResult
        )

        LaunchedEffect(viewModel, snackbarController, context) {
            viewModel.events.collect { event ->
                when (event) {
                    is BackupSettingsViewModel.BackupEvent.ShowUiMessage -> {
                        snackbarController.showSnackbar(
                            event.uiText.asString(context),
                            event.uiText.isErrorText
                        )
                    }

                    is BackupSettingsViewModel.BackupEvent.LaunchGoogleSignIn -> {
                        googleSignInLauncher.launch(event.intent)
                    }
                }
            }
        }

        BackupSettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp
        )
    }
}