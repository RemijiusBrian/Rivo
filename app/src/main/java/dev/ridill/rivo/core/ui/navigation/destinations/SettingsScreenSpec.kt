package dev.ridill.rivo.core.ui.navigation.destinations

import android.Manifest
import android.content.Intent
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
import dev.ridill.rivo.core.ui.components.FloatingWindowNavigationResultEffect
import dev.ridill.rivo.core.ui.components.rememberPermissionState
import dev.ridill.rivo.core.ui.components.rememberSnackbarController
import dev.ridill.rivo.core.ui.util.UiText
import dev.ridill.rivo.core.ui.util.launchAppNotificationSettings
import dev.ridill.rivo.core.ui.util.launchAppSettings
import dev.ridill.rivo.settings.presentation.settings.SettingsScreen
import dev.ridill.rivo.settings.presentation.settings.SettingsViewModel

data object SettingsScreenSpec : ScreenSpec {

    override val route: String = "settings"

    override val labelRes: Int = R.string.destination_settings

    @Composable
    override fun Content(
        windowSizeClass: WindowSizeClass,
        navController: NavHostController,
        navBackStackEntry: NavBackStackEntry
    ) {
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val smsPermissionState = rememberPermissionState(
            permission = Manifest.permission.RECEIVE_SMS,
            onPermissionResult = viewModel::onSmsPermissionResult
        )

        val context = LocalContext.current
        val snackbarController = rememberSnackbarController()

        CollectFlowEffect(viewModel.events, snackbarController, context) { event ->
            when (event) {
                is SettingsViewModel.SettingsEvent.ShowUiMessage -> {
                    snackbarController.showSnackbar(
                        event.uiText.asString(context),
                        event.uiText.isErrorText
                    )
                }

                SettingsViewModel.SettingsEvent.RequestSMSPermission -> {
                    smsPermissionState.launchRequest()
                }

                SettingsViewModel.SettingsEvent.LaunchAppSettings -> {
                    context.launchAppSettings()
                }
            }
        }

        FloatingWindowNavigationResultEffect<String>(
            resultKey = UpdateBudgetSheetSpec.UPDATE_BUDGET_RESULT,
            navBackStackEntry = navBackStackEntry,
            viewModel,
        ) { result ->
            when (result) {
                UpdateBudgetSheetSpec.RESULT_BUDGET_UPDATED -> {
                    snackbarController.showSnackbar(
                        UiText.StringResource(R.string.budget_updated).asString(context)
                    )
                }
            }
        }

        FloatingWindowNavigationResultEffect<String>(
            resultKey = UpdateCurrencyPreferenceSheetSpec.UPDATE_CURRENCY_RESULT,
            navBackStackEntry = navBackStackEntry,
            viewModel,
            snackbarController,
            context
        ) { result ->
            when (result) {
                UpdateCurrencyPreferenceSheetSpec.RESULT_CURRENCY_UPDATED -> {
                    snackbarController.showSnackbar(
                        UiText.StringResource(R.string.currency_updated).asString(context)
                    )
                }
            }
        }

        SettingsScreen(
            snackbarController = snackbarController,
            state = state,
            actions = viewModel,
            navigateUp = navController::navigateUp,
            navigateToAccountDetails = { navController.navigate(AccountDetailsScreenSpec.route) },
            navigateToNotificationSettings = context::launchAppNotificationSettings,
            navigateToBackupSettings = { navController.navigate(BackupSettingsScreenSpec.route) },
            navigateToSecuritySettings = { navController.navigate(SecuritySettingsScreenSpec.route) },
            navigateToUpdateBudget = { navController.navigate(UpdateBudgetSheetSpec.route) },
            navigateToUpdateCurrency = { navController.navigate(UpdateCurrencyPreferenceSheetSpec.route) },
            launchUriInBrowser = {
                val intent = Intent(Intent.ACTION_VIEW, it)
                context.startActivity(intent)
            }
        )
    }
}