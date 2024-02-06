package dev.ridill.rivo.core.ui.navigation.destinations

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import dev.ridill.rivo.R
import dev.ridill.rivo.core.ui.components.slideInHorizontallyWithFadeIn
import dev.ridill.rivo.core.ui.components.slideOutHorizontallyWithFadeOut
import dev.ridill.rivo.settings.domain.appLock.AppAutoLockInterval
import dev.ridill.rivo.settings.presentation.security.SecuritySettingsScreen
import dev.ridill.rivo.settings.presentation.security.SecuritySettingsViewModel
import dev.ridill.rivo.settings.presentation.security.rememberBiometricPrompt
import dev.ridill.rivo.settings.presentation.security.rememberPromptInfo

data object SecuritySettingsScreenSpec : ScreenSpec {
    override val route: String = "security_settings"

    override val labelRes: Int = R.string.destination_security_settings

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? =
        { slideOutHorizontallyWithFadeOut { it } }

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? =
        { slideInHorizontallyWithFadeIn { it } }

    @Composable
    override fun Content(navController: NavHostController, navBackStackEntry: NavBackStackEntry) {
        val viewModel: SecuritySettingsViewModel = hiltViewModel(navBackStackEntry)
        val appLockEnabled by viewModel.appLockEnabled.collectAsStateWithLifecycle(false)
        val selectedInterval by viewModel.appAutoLockInterval
            .collectAsStateWithLifecycle(AppAutoLockInterval.ONE_MINUTE)
        val biometricPrompt = rememberBiometricPrompt(
            onAuthSucceeded = { viewModel.onAuthenticationSuccess() }
        )
        val promptInfo = rememberPromptInfo(
            title = stringResource(R.string.fingerprint_title),
            subTitle = stringResource(R.string.fingerprint_subtitle)
        )

        LaunchedEffect(biometricPrompt, promptInfo) {
            viewModel.events.collect { event ->
                when (event) {
                    SecuritySettingsViewModel.SecuritySettingsEvent.LaunchAuthentication -> {
                        biometricPrompt.authenticate(promptInfo)
                    }
                }
            }
        }

        SecuritySettingsScreen(
            appLockEnabled = appLockEnabled,
            onAppLockToggle = viewModel::onAppLockToggle,
            autoLockInterval = selectedInterval,
            onIntervalSelect = viewModel::onAutoLockIntervalSelect,
            navigateUp = navController::navigateUp
        )
    }
}