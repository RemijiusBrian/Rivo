package dev.ridill.mym.core.ui.navigation.destinations

import android.Manifest
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.mym.R
import dev.ridill.mym.core.ui.components.rememberPermissionState
import dev.ridill.mym.core.ui.components.rememberSnackbarController
import dev.ridill.mym.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.mym.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.mym.core.ui.util.launchAppNotificationSettings
import dev.ridill.mym.core.ui.util.launchAppSettings
import dev.ridill.mym.settings.presentation.settings.SettingsScreen
import dev.ridill.mym.settings.presentation.settings.SettingsViewModel

object SettingsScreenSpec : ScreenSpec {

    override val route: String = "settings"

    override val labelRes: Int = R.string.destination_settings

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { -it } }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { -it } }

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: SettingsViewModel = hiltViewModel(navBackStackEntry)
        val state by viewModel.state.collectAsStateWithLifecycle()

        val smsPermissionState = rememberPermissionState(
            permission = Manifest.permission.RECEIVE_SMS,
            onPermissionResult = viewModel::onSmsPermissionResult
        )

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

                    SettingsViewModel.SettingsEvent.RequestSMSPermission -> {
                        smsPermissionState.launchRequest()
                    }

                    SettingsViewModel.SettingsEvent.LaunchAppSettings -> {
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
            navigateToBackupSettings = { navController.navigate(BackupSettingsScreenSpec.route) }
        )
    }
}